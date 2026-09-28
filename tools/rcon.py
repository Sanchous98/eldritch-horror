#!/usr/bin/env python3
"""Minimal RCON client to drive a running Minecraft dev server in tests."""
import socket
import struct
import sys


def send(sock, rid, typ, payload):
    body = struct.pack("<ii", rid, typ) + payload.encode("utf8") + b"\x00\x00"
    sock.sendall(struct.pack("<i", len(body)) + body)


def recv(sock):
    raw = sock.recv(4)
    if len(raw) < 4:
        return None, None
    (ln,) = struct.unpack("<i", raw)
    data = b""
    while len(data) < ln:
        data += sock.recv(ln - len(data))
    rid, typ = struct.unpack("<ii", data[:8])
    return rid, data[8:-2].decode("utf8", "replace")


def main():
    host, port, password = sys.argv[1], int(sys.argv[2]), sys.argv[3]
    commands = sys.argv[4:]
    s = socket.create_connection((host, port), timeout=15)
    send(s, 1, 3, password)
    rid, _ = recv(s)
    if rid == -1:
        sys.exit("rcon auth failed")
    for cmd in commands:
        send(s, 2, 2, cmd)
        _, resp = recv(s)
        print(f"> {cmd}\n  {resp.strip()}")


if __name__ == "__main__":
    main()
