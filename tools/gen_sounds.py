#!/usr/bin/env python3
"""Procedural OGG sound-effect generator (design/07-horror-atmosphere, design/05-ritual-engine).

Synthesises every mod-specific sound from code, so the pack is reproducible: the recipe is this
file, not the binary. Deterministic (fixed RNG seeds), mono 16-bit / 44.1 kHz, encoded to `.ogg`
with ffmpeg (the `linuxserver/ffmpeg` container when no host ffmpeg exists — set `DOCKER_HOST`
in that case, e.g. `unix:///home/dev/.docker-run/docker.sock`).

Idempotent: files that already exist are skipped unless `--force` is given, so re-running after
adding a sound only renders the new one.

Usage:
    python3 tools/gen_sounds.py            # render everything missing
    python3 tools/gen_sounds.py --force    # re-render everything
    python3 tools/gen_sounds.py --list     # show the pack and where each sound is used
    python3 tools/gen_sounds.py --only ritual.success

The event names (second column) become entries in
`src/main/resources/assets/eldritch_horror/sounds.json` and `registry/ModSounds.java`.
"""

from __future__ import annotations

import argparse
import math
import os
import random
import shutil
import subprocess
import sys
import tempfile
import wave
from array import array
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/main/resources/assets/eldritch_horror/sounds"

SR = 44100
TAU = 2.0 * math.pi
SIN = math.sin
EXP = math.exp
CLAMP = 32767


# --- tiny DSP toolkit --------------------------------------------------------------------------


class Track:
    """A mono float buffer plus the few filters the recipes below need."""

    def __init__(self, seconds: float):
        self.sr = SR
        self.n = int(seconds * SR)
        self.buf = array("f", [0.0]) * self.n

    def osc(self, freq, amp, harm=((1.0, 1.0),), drift=0.0, seed=7):
        """Additive tone. `freq`/`amp` are callables of t (seconds); `harm` is (multiple, weight);
        `drift` detunes the partials apart by up to that many cycles/second per second."""
        buf = self.buf
        rng = random.Random(seed)
        phase = [rng.uniform(0.0, TAU) for _ in harm]
        wobble = [rng.uniform(-1.0, 1.0) * drift for _ in harm]
        for i in range(self.n):
            t = i / SR
            a = amp(t)
            if a <= 0.0:
                continue
            f = freq(t)
            s = 0.0
            for k, (mult, weight) in enumerate(harm):
                phase[k] += TAU * f * mult / SR
                s += weight * SIN(phase[k] + wobble[k] * t)
            buf[i] += a * s
        return self

    def noise(self, amp, lp=None, hp=None, seed=11):
        """Seeded white noise through a one-pole lowpass and/or highpass; `amp` is a callable."""
        buf = self.buf
        rng = random.Random(seed)
        al = None if lp is None else 1.0 - EXP(-TAU * lp / SR)
        ah = None if hp is None else 1.0 - EXP(-TAU * hp / SR)
        y_lp = 0.0
        y_hp = 0.0
        gen = rng.uniform
        for i in range(self.n):
            x = gen(-1.0, 1.0)
            if al is not None:
                y_lp += al * (x - y_lp)
                x = y_lp
            if ah is not None:
                y_hp += ah * (x - y_hp)
                x -= y_hp
            buf[i] += amp(i / SR) * x
        return self

    def room(self, mix=0.35, taps=((0.061, 0.62), (0.089, 0.45), (0.127, 0.33), (0.181, 0.22))):
        """Cheap spaciousness: a few decaying delay taps off a copy of the dry signal."""
        buf = self.buf
        dry = array("f", buf)
        for delay, gain in taps:
            d = int(delay * SR)
            g = mix * gain
            for i in range(d, self.n):
                buf[i] += g * dry[i - d]
        return self

    def finish(self, target_db=-3.0):
        """Normalise to `target_db` and soft-clip (tanh), so the mix never crackles."""
        buf = self.buf
        peak = max((abs(v) for v in buf), default=0.0) or 1.0
        gain = (10.0 ** (target_db / 20.0)) / peak
        knee = math.tanh(1.7)
        for i, v in enumerate(buf):
            buf[i] = math.tanh(1.7 * v * gain) / knee
        return self


def envelope(t, dur, attack, release):
    """Trapezoid with smooth (smoothstep) edges."""
    if t < attack:
        x = t / attack
        return x * x * (3.0 - 2.0 * x)
    if t > dur - release:
        x = max(0.0, (dur - t) / release)
        return x * x * (3.0 - 2.0 * x)
    return 1.0


def env(dur, attack, release):
    return lambda t: envelope(t, dur, attack, release)


def strike(t, attack, tau):
    """Percussive envelope: smooth attack, exponential decay with time constant `tau`."""
    if t < attack:
        x = t / attack
        return (x * x * (3.0 - 2.0 * x)) * EXP(-t / tau)
    return EXP(-t / tau)


def bursts(dur, centres, width, level):
    """Syllable-like amplitude: a sum of gaussian bumps centred at `centres`."""
    def f(t):
        s = 0.0
        for c in centres:
            d = (t - c) / width
            s += EXP(-d * d)
        return level * min(1.0, s)
    return f


def pulse(t, rate, phase=0.0):
    """A slow unipolar LFO in [0, 1]."""
    return 0.5 + 0.5 * SIN(TAU * rate * t + phase)


# --- the pack ----------------------------------------------------------------------------------
# name -> (relative ogg path, event name, one-line "what plays it", recipe)

SOUNDS: dict[str, tuple] = {}


def sound(name, path, event, use):
    def decorate(fn):
        SOUNDS[name] = (path, event, use, fn)
        return fn
    return decorate


@sound("ritual.hum", "ritual/ritual_hum", "ritual.hum",
       "a working altar's drone, played near an altar that has a rite ready")
def _ritual_hum():
    dur = 8.0
    tr = Track(dur)
    e = env(dur, 1.5, 1.5)
    tr.osc(lambda t: 55.0, lambda t: 0.50 * e(t), harm=((1, 1.0), (2, 0.35), (3, 0.12)), drift=0.4)
    tr.osc(lambda t: 82.5 + 0.35 * SIN(TAU * 0.11 * t), lambda t: 0.26 * e(t),
           harm=((1, 1.0), (2, 0.20)), drift=0.3, seed=13)
    tr.noise(lambda t: 0.13 * e(t) * (0.6 + 0.4 * pulse(t, 0.07)), lp=420.0, seed=3)
    tr.osc(lambda t: 1320.0, lambda t: 0.018 * e(t) * pulse(t, 0.7), drift=0.5, seed=21)
    return tr.room(0.40)


@sound("ritual.success", "ritual/ritual_success", "ritual.success",
       "a rite resolves: a bell of stacked partials, a rising sweep and a sub-thump")
def _ritual_success():
    dur = 3.2
    tr = Track(dur)
    for mult, tau, weight, seed in ((1, 1.6, 0.50, 31), (2, 1.1, 0.30, 32), (3, 0.9, 0.18, 33),
                                    (4, 0.7, 0.10, 34), (6, 0.5, 0.06, 35)):
        tr.osc(lambda t, m=mult: 220.0 * m * (1.0 + 0.0007 * SIN(TAU * 5.0 * t)),
               lambda t, w=weight, tt=tau: w * strike(t, 0.004, tt), drift=0.2, seed=seed)
    tr.osc(lambda t: 180.0 * (1.0 + 3.2 * min(1.0, t / 1.2)),
           lambda t: 0.20 * envelope(t, dur, 0.05, 0.4) if t < 1.2 else 0.0, seed=41)
    tr.osc(lambda t: 58.0, lambda t: 0.50 * strike(t, 0.002, 0.5), seed=42)
    tr.noise(lambda t: 0.22 * strike(t, 0.002, 0.22), hp=3000.0, seed=5)
    return tr.room(0.28)


@sound("ritual.failure", "ritual/ritual_failure", "ritual.failure",
       "a rite refuses: a detuned low cluster sinking under a downward sweep")
def _ritual_failure():
    dur = 2.6
    tr = Track(dur)
    e = env(dur, 0.30, 0.9)
    for f0, weight, seed in ((70.0, 0.34, 51), (74.0, 0.30, 52), (78.0, 0.26, 53)):
        tr.osc(lambda t, f=f0: f, lambda t, w=weight: w * e(t), harm=((1, 1.0), (3, 0.18)), seed=seed)
    tr.osc(lambda t: 300.0 - 210.0 * min(1.0, t / 1.2),
           lambda t: 0.24 * (envelope(t, dur, 0.02, 0.3) if t < 1.2 else 0.0), seed=54)
    tr.noise(lambda t: 0.20 * strike(t, 0.001, 0.12), lp=900.0, seed=6)
    return tr.room(0.30)


@sound("endgame.awaken", "endgame/awaken", "endgame.awaken",
       "the Horror walks: a huge horn swell with a distant choir (stage clock starts)")
def _endgame_awaken():
    dur = 7.0
    tr = Track(dur)
    e = env(dur, 2.6, 2.0)
    tr.osc(lambda t: 55.0, lambda t: 0.55 * e(t),
           harm=((1, 1.0), (2, 0.5), (3, 0.33), (4, 0.25), (5, 0.2), (6, 0.16)), drift=0.5)
    for f0, weight, rate, seed in ((220.0, 0.16, 0.23, 61), (277.18, 0.12, 0.31, 62),
                                   (329.63, 0.10, 0.19, 63)):
        tr.osc(lambda t, f=f0: f + 0.7 * SIN(TAU * 5.2 * t), lambda t, w=weight: w * e(t),
               harm=((1, 1.0), (2, 0.25)), drift=0.4, seed=seed)
    tr.noise(lambda t: 0.10 * e(t) * pulse(t, 0.05, 1.3), lp=300.0, seed=7)
    return tr.room(0.55, taps=((0.093, 0.65), (0.141, 0.5), (0.207, 0.38), (0.317, 0.26)))


@sound("endgame.banish", "endgame/banish", "endgame.banish",
       "the banishment: a sealing impact, a rising harmonic and a long air-rush release")
def _endgame_banish():
    dur = 4.0
    tr = Track(dur)
    tr.osc(lambda t: 45.0, lambda t: 0.55 * strike(t, 0.002, 1.1), harm=((1, 1.0), (2, 0.3)), seed=71)
    tr.noise(lambda t: 0.30 * strike(t, 0.001, 0.6), lp=900.0, seed=8)
    tr.osc(lambda t: 330.0 * (1.0 + 2.0 * min(1.0, t / 0.7)),
           lambda t: 0.22 * (1.0 - t / 0.7) if t < 0.7 else 0.0, seed=72)
    tr.noise(lambda t: 0.16 * envelope(t, dur, 0.3, 1.6) if t > 0.25 else 0.0, hp=1500.0, seed=9)
    return tr.room(0.35)


@sound("endgame.summon", "endgame/summon", "endgame.summon",
       "the summoning: a slow bell toll under a growling low swell (the corruption floor)")
def _endgame_summon():
    dur = 6.5
    tr = Track(dur)
    for mult, tau, weight, seed in ((1, 3.6, 0.42, 81), (2, 2.4, 0.22, 82), (3, 1.6, 0.12, 83),
                                    (4, 1.1, 0.07, 84)):
        tr.osc(lambda t, m=mult: 110.0 * m, lambda t, w=weight, tt=tau: w * strike(t, 0.006, tt),
               seed=seed)
    e = env(dur, 1.8, 2.2)
    tr.osc(lambda t: 55.0 * (1.0 + 0.01 * SIN(TAU * 5.5 * t)),
           lambda t: 0.42 * e(t) * (0.72 + 0.28 * pulse(t, 5.5)),
           harm=((1, 1.0), (2, 0.4), (3, 0.22), (4, 0.12)), drift=0.6, seed=85)
    return tr.room(0.60, taps=((0.093, 0.65), (0.141, 0.5), (0.207, 0.38), (0.317, 0.26)))


@sound("entity.the_horror.ambient", "entity/the_horror_ambient", "entity.the_horror.ambient",
       "the Horror itself: layered breathing over a 38 Hz drone")
def _horror_ambient():
    dur = 5.0
    tr = Track(dur)
    e = env(dur, 0.8, 1.2)
    tr.noise(lambda t: 0.30 * e(t) * (0.25 + 0.75 * pulse(t, 0.22)), lp=700.0, hp=200.0, seed=12)
    tr.noise(lambda t: 0.24 * e(t) * (0.25 + 0.75 * pulse(t, 0.22, math.pi)), lp=520.0, hp=140.0,
             seed=13)
    tr.osc(lambda t: 38.0, lambda t: 0.34 * e(t), harm=((1, 1.0), (2, 0.22)), drift=0.3, seed=91)
    tr.osc(lambda t: 160.0 * (1.0 + 0.04 * SIN(TAU * 0.6 * t)), lambda t: 0.05 * e(t) * pulse(t, 0.4),
           seed=92)
    return tr.room(0.35)


@sound("entity.the_horror.stage", "entity/the_horror_stage", "entity.the_horror.stage",
       "a stage changes: a heartbeat, a rising dread sweep and a whisper under it")
def _horror_stage():
    dur = 4.0
    tr = Track(dur)
    tr.osc(lambda t: 55.0, lambda t: 0.40 * strike(t, 0.002, 0.22), seed=101)
    tr.osc(lambda t: 55.0, lambda t: 0.26 * strike(max(0.0, t - 1.1), 0.002, 0.20) if t > 1.1 else 0.0,
           seed=102)
    tr.osc(lambda t: 80.0 + 220.0 * min(1.0, t / 3.4), lambda t: 0.20 * env(dur, 0.4, 1.0)(t)
           * (0.6 + 0.4 * pulse(t, 6.0)), harm=((1, 1.0), (3, 0.15)), drift=0.5, seed=103)
    tr.noise(bursts(dur, (1.4, 1.9, 2.5, 2.9), 0.14, 0.14), lp=2400.0, hp=600.0, seed=14)
    return tr.room(0.40)


@sound("entity.the_horror.hurt", "entity/the_horror_hurt", "entity.the_horror.hurt",
       "the Horror is struck: a descending horn with a breathy grunt")
def _horror_hurt():
    dur = 2.2
    tr = Track(dur)
    tr.osc(lambda t: 150.0 - 70.0 * min(1.0, t / 0.5), lambda t: 0.45 * strike(t, 0.003, 0.45),
           harm=((1, 1.0), (2, 0.5), (3, 0.25)), drift=0.6, seed=131)
    tr.noise(lambda t: 0.22 * strike(t, 0.002, 0.35), lp=750.0, hp=180.0, seed=17)
    tr.osc(lambda t: 50.0, lambda t: 0.35 * strike(t, 0.002, 0.3), seed=132)
    return tr.room(0.35)


@sound("entity.the_horror.death", "entity/the_horror_death", "entity.the_horror.death",
       "the Horror goes down: a long descending collapse into the room's tail")
def _horror_death():
    dur = 3.5
    tr = Track(dur)
    tr.osc(lambda t: 95.0 - 55.0 * min(1.0, t / 1.5), lambda t: 0.50 * env(dur, 0.01, 1.6)(t),
           harm=((1, 1.0), (2, 0.45), (3, 0.22), (4, 0.12)), drift=0.7, seed=141)
    tr.noise(lambda t: 0.30 * strike(t, 0.002, 0.8), lp=650.0, seed=18)
    tr.osc(lambda t: 42.0, lambda t: 0.30 * strike(t, 0.002, 1.2), seed=142)
    tr.noise(lambda t: 0.10 * envelope(t, dur, 0.2, 1.4) if t > 0.5 else 0.0, hp=1800.0, seed=19)
    return tr.room(0.60, taps=((0.093, 0.65), (0.141, 0.5), (0.207, 0.38), (0.317, 0.26)))


@sound("ambient.whisper", "ambient/whisper", "ambient.whisper",
       "sanity pressure: breathy syllables that never resolve into words")
def _whisper():
    dur = 3.0
    tr = Track(dur)
    tr.noise(bursts(dur, (0.35, 0.75, 1.25, 1.5, 2.2), 0.11, 0.30), lp=2400.0, hp=600.0, seed=15)
    tr.osc(lambda t: 120.0 + 3.0 * SIN(TAU * 4.5 * t),
           lambda t: 0.05 * bursts(dur, (0.4, 1.3, 2.2), 0.2, 1.0)(t), harm=((1, 1.0), (2, 0.3)),
           seed=111)
    return tr.room(0.45)


@sound("ambient.dread", "ambient/dread", "ambient.dread",
       "corruption ambience: a low three-note pad with a dissonant shimmer")
def _dread():
    dur = 8.0
    tr = Track(dur)
    e = env(dur, 2.2, 2.6)
    for f0, weight, seed in ((60.0, 0.34, 121), (90.0, 0.22, 122), (120.0, 0.14, 123)):
        tr.osc(lambda t, f=f0: f, lambda t, w=weight: w * e(t), harm=((1, 1.0), (2, 0.22)), seed=seed)
    mid = lambda t: 0.035 * pulse(t, 0.09) * (1.0 - abs(2.0 * min(1.0, t / dur) - 1.0))
    tr.osc(lambda t: 620.0, mid, drift=0.8, seed=124)
    tr.osc(lambda t: 655.0, mid, drift=0.8, seed=125)
    tr.noise(lambda t: 0.06 * e(t) * pulse(t, 0.05, 1.1), lp=250.0, seed=16)
    return tr.room(0.50, taps=((0.093, 0.65), (0.141, 0.5), (0.207, 0.38), (0.317, 0.26)))


# --- rendering ---------------------------------------------------------------------------------


def find_ffmpeg() -> list[str]:
    if shutil.which("ffmpeg"):
        return ["ffmpeg"]
    return [
        "docker", "run", "--rm", "-v", "{dir}:/w", "linuxserver/ffmpeg",
        "-y", "-hide_banner", "-loglevel", "error", "-i", "/w/{name}.wav",
        "-c:a", "libvorbis", "-q:a", "5", "-ac", "1", "/w/{name}.ogg",
    ]


def write_wav(track: Track, path: Path) -> None:
    pcm = array("h", [0]) * track.n
    for i, v in enumerate(track.buf):
        x = int(v * CLAMP)
        pcm[i] = -CLAMP if x < -CLAMP else (CLAMP if x > CLAMP else x)
    with wave.open(str(path), "wb") as wav:
        wav.setnchannels(1)
        wav.setsampwidth(2)
        wav.setframerate(SR)
        wav.writeframes(pcm.tobytes())


def render(name: str, force: bool, tmp: Path) -> str:
    path, event, _use, recipe = SOUNDS[name]
    ogg = OUT / f"{path}.ogg"
    if ogg.exists() and not force:
        return f"skip  {path}.ogg (exists)"
    track = recipe().finish()
    wav = tmp / f"{name}.wav"
    write_wav(track, wav)
    cmd = [part.format(dir=str(tmp), name=name) for part in find_ffmpeg()]
    proc = subprocess.run(cmd, capture_output=True, text=True)
    if proc.returncode != 0:
        raise RuntimeError(f"ffmpeg failed for {name}: {proc.stderr.strip()[:400]}")
    ogg.parent.mkdir(parents=True, exist_ok=True)
    shutil.move(str(tmp / f"{name}.ogg"), str(ogg))
    size = ogg.stat().st_size // 1024
    return f"write {path}.ogg  ({track.n / SR:.1f}s, {size} KiB)  ← {event}"


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--force", action="store_true", help="re-render existing files")
    parser.add_argument("--only", metavar="NAME", help="render one sound (e.g. ritual.success)")
    parser.add_argument("--list", action="store_true", help="list the pack and exit")
    args = parser.parse_args(argv)

    if args.list:
        for name, (path, event, use, _fn) in SOUNDS.items():
            print(f"{name:28} {path + '.ogg':34} {use}")
        return 0

    names = [args.only] if args.only else list(SOUNDS)
    unknown = [n for n in names if n not in SOUNDS]
    if unknown:
        print(f"unknown sound(s): {', '.join(unknown)}", file=sys.stderr)
        return 2

    tmp_root = next(
        (d for d in (Path("/tmp/opencode"), Path(tempfile.gettempdir()))
         if d.is_dir() and os.access(d, os.W_OK)),
        None,
    )
    tmp = Path(tempfile.mkdtemp(prefix="eh_sounds_", dir=tmp_root))
    try:
        for name in names:
            print(render(name, args.force, tmp))
    finally:
        shutil.rmtree(tmp, ignore_errors=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
