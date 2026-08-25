import math
import os
import random
import struct
import wave

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "assets", "AUDIO")
RATE = 44100


def frames(seconds):
    return int(RATE * seconds)


def silence(seconds):
    return [0.0] * frames(seconds)


def tone(seconds, start, end=None, shape="sine", detune=0.0, wobble=0.0, wobble_hz=0.0):
    end = start if end is None else end
    count = frames(seconds)
    out = [0.0] * count
    phase = 0.0
    for i in range(count):
        t = i / count
        freq = start * (end / start) ** t if start > 0 and end > 0 else start
        if wobble:
            freq *= 1.0 + wobble * math.sin(2 * math.pi * wobble_hz * i / RATE)
        freq *= 1.0 + detune
        phase += 2 * math.pi * freq / RATE
        if shape == "sine":
            out[i] = math.sin(phase)
        elif shape == "saw":
            cycle = (phase / (2 * math.pi)) % 1.0
            out[i] = 2.0 * cycle - 1.0
        elif shape == "square":
            out[i] = 1.0 if math.sin(phase) >= 0 else -1.0
        else:
            out[i] = math.sin(phase) + 0.35 * math.sin(2 * phase)
    return out


def noise(seconds, seed=1):
    rng = random.Random(seed)
    return [rng.uniform(-1.0, 1.0) for _ in range(frames(seconds))]


def lowpass(signal, cutoff, sweep_to=None):
    out = [0.0] * len(signal)
    value = 0.0
    for i, sample in enumerate(signal):
        hz = cutoff
        if sweep_to:
            t = i / max(1, len(signal) - 1)
            hz = cutoff * (sweep_to / cutoff) ** t
        alpha = 1.0 - math.exp(-2 * math.pi * hz / RATE)
        value += alpha * (sample - value)
        out[i] = value
    return out


def highpass(signal, cutoff):
    out = [0.0] * len(signal)
    previous = 0.0
    value = 0.0
    alpha = 1.0 / (1.0 + 2 * math.pi * cutoff / RATE)
    for i, sample in enumerate(signal):
        value = alpha * (value + sample - previous)
        previous = sample
        out[i] = value
    return out


def bandpass(signal, low, high, sweep_to=None):
    return highpass(lowpass(signal, high, sweep_to), low)


def envelope(signal, attack=0.005, hold=0.0, curve=4.0):
    count = len(signal)
    rise = max(1, frames(attack))
    flat = frames(hold)
    out = [0.0] * count
    for i, sample in enumerate(signal):
        if i < rise:
            gain = i / rise
        elif i < rise + flat:
            gain = 1.0
        else:
            t = (i - rise - flat) / max(1, count - rise - flat)
            gain = math.exp(-curve * t)
        out[i] = sample * gain
    return out


def swell(signal, curve=2.0):
    count = len(signal)
    return [s * math.sin(math.pi * (i / count)) ** curve for i, s in enumerate(signal)]


def tremolo(signal, hz, depth=0.5):
    return [s * (1.0 - depth + depth * (0.5 + 0.5 * math.sin(2 * math.pi * hz * i / RATE)))
            for i, s in enumerate(signal)]


def gain(signal, amount):
    return [s * amount for s in signal]


def mix(*signals):
    length = max(len(s) for s in signals)
    out = [0.0] * length
    for signal in signals:
        for i, sample in enumerate(signal):
            out[i] += sample
    return out


def after(delay, signal):
    return silence(delay) + signal


def normalize(signal, peak=0.86):
    loudest = max((abs(s) for s in signal), default=0.0)
    if loudest < 1e-6:
        return signal
    scale = peak / loudest
    return [s * scale for s in signal]


def polish(signal, peak=0.86):
    signal = normalize(signal, peak)
    tail = min(len(signal), frames(0.01))
    for i in range(tail):
        signal[len(signal) - tail + i] *= 1.0 - i / tail
    return signal


def save(name, signal, peak=0.86):
    signal = polish(signal, peak)
    data = b"".join(struct.pack("<h", int(max(-1.0, min(1.0, s)) * 32767)) for s in signal)
    path = os.path.join(OUT, name + ".wav")
    with wave.open(path, "wb") as handle:
        handle.setnchannels(1)
        handle.setsampwidth(2)
        handle.setframerate(RATE)
        handle.writeframes(data)
    return path, len(data)


def shoot():
    body = envelope(tone(0.11, 700, 300), attack=0.002, curve=7.0)
    puff = envelope(lowpass(noise(0.07, 11), 2600), attack=0.001, curve=9.0)
    return mix(gain(body, 0.75), gain(puff, 0.5))


def lob():
    air = envelope(bandpass(noise(0.28, 12), 500, 1200, sweep_to=3000), attack=0.03, curve=2.5)
    hum = envelope(tone(0.28, 260, 520), attack=0.03, curve=3.0)
    return mix(gain(air, 0.8), gain(hum, 0.3))


def splat():
    wet = envelope(lowpass(noise(0.16, 13), 1500), attack=0.001, curve=9.0)
    thud = envelope(tone(0.12, 240, 90), attack=0.001, curve=8.0)
    return mix(gain(wet, 0.85), gain(thud, 0.45))


def explode():
    boom = envelope(lowpass(noise(0.85, 14), 900, sweep_to=180), attack=0.002, curve=4.5)
    drop = envelope(tone(0.7, 130, 38), attack=0.002, curve=4.0)
    crack = envelope(highpass(noise(0.09, 15), 2400), attack=0.001, curve=10.0)
    return mix(gain(boom, 0.9), gain(drop, 0.7), gain(crack, 0.4))


def plant():
    dirt = envelope(lowpass(noise(0.22, 16), 1100), attack=0.002, curve=7.0)
    pat = envelope(tone(0.16, 190, 110), attack=0.003, curve=6.0)
    return mix(gain(dirt, 0.7), gain(pat, 0.5))


def shovel():
    scrape = tremolo(bandpass(noise(0.34, 17), 900, 5200), 34, 0.6)
    return envelope(scrape, attack=0.02, curve=3.0)


def gulp():
    glug = tremolo(tone(0.38, 620, 150, wobble=0.06, wobble_hz=17), 13, 0.65)
    return envelope(glug, attack=0.01, curve=3.2)


def sun():
    ring = mix(gain(tone(0.75, 1046), 1.0), gain(tone(0.75, 1568), 0.55),
               gain(tone(0.75, 2093), 0.3))
    return envelope(ring, attack=0.004, curve=4.5)


def mower():
    engine = tone(1.1, 92, 104, shape="saw", wobble=0.05, wobble_hz=11)
    rattle = gain(lowpass(noise(1.1, 18), 2200), 0.35)
    return envelope(mix(engine, rattle), attack=0.06, hold=0.75, curve=3.0)


def click():
    return envelope(tone(0.045, 1500, 900), attack=0.001, curve=11.0)


def bite():
    first = envelope(lowpass(noise(0.09, 19), 1700), attack=0.001, curve=9.0)
    second = envelope(lowpass(noise(0.1, 20), 1400), attack=0.001, curve=8.0)
    return mix(gain(first, 0.8), after(0.11, gain(second, 0.9)))


def zombie_dies():
    groan = tone(0.6, 210, 78, shape="saw", wobble=0.07, wobble_hz=8)
    return envelope(lowpass(groan, 1500), attack=0.02, curve=3.0)


def armour_breaks():
    clang = mix(gain(tone(0.34, 1180), 1.0), gain(tone(0.34, 1790), 0.7),
                gain(tone(0.34, 2570), 0.45))
    hit = envelope(highpass(noise(0.06, 21), 2000), attack=0.001, curve=11.0)
    return mix(envelope(clang, attack=0.002, curve=6.0), gain(hit, 0.6))


def plant_dies():
    wilt = tone(0.42, 520, 170, wobble=0.03, wobble_hz=9)
    rustle = envelope(bandpass(noise(0.3, 22), 1400, 4200), attack=0.01, curve=5.0)
    return mix(gain(envelope(wilt, attack=0.01, curve=3.5), 0.75), gain(rustle, 0.35))


def grave_breaks():
    crack = envelope(highpass(noise(0.12, 23), 1600), attack=0.001, curve=8.0)
    rubble = envelope(lowpass(noise(0.4, 24), 900), attack=0.004, curve=4.0)
    thump = envelope(tone(0.25, 150, 60), attack=0.002, curve=5.0)
    return mix(gain(crack, 0.8), gain(rubble, 0.6), gain(thump, 0.5))


def rise():
    rumble = envelope(lowpass(noise(0.65, 25), 200, sweep_to=1200), attack=0.05, curve=2.5)
    lift = envelope(tone(0.65, 70, 190), attack=0.05, curve=2.5)
    return mix(gain(rumble, 0.8), gain(lift, 0.5))


def storm():
    wind = swell(bandpass(noise(1.4, 26), 400, 2600, sweep_to=1200), curve=1.4)
    return gain(wind, 0.9)


def tide():
    wash = swell(lowpass(noise(1.3, 27), 2400, sweep_to=500), curve=1.2)
    return gain(wash, 0.9)


def mint():
    sparkle = mix(
        envelope(tone(0.5, 784), attack=0.003, curve=6.0),
        after(0.09, envelope(tone(0.44, 1046), attack=0.003, curve=6.0)),
        after(0.18, envelope(tone(0.4, 1318), attack=0.003, curve=6.0)),
        after(0.27, envelope(tone(0.38, 1568), attack=0.003, curve=5.0)))
    return gain(sparkle, 0.7)


def wave_call():
    call = mix(envelope(tone(0.55, 440, 660), attack=0.02, curve=3.0),
               gain(envelope(tone(0.55, 220, 330), attack=0.02, curve=3.0), 0.5))
    return call


def huge_wave():
    call = mix(envelope(tone(1.1, 330, 740), attack=0.04, curve=2.0),
               gain(envelope(tone(1.1, 165, 370), attack=0.04, curve=2.0), 0.6))
    rumble = envelope(lowpass(noise(1.1, 28), 320), attack=0.05, curve=2.0)
    return mix(gain(call, 0.85), gain(rumble, 0.55))


def boss_move():
    stomp = envelope(tone(0.5, 88, 42), attack=0.002, curve=4.0)
    servo = envelope(bandpass(noise(0.45, 29), 700, 3000), attack=0.01, curve=4.0)
    return mix(gain(stomp, 0.9), gain(servo, 0.45))


def boss_hurt():
    hit = envelope(mix(gain(tone(0.45, 620, 210), 1.0), gain(tone(0.45, 930, 320), 0.6)),
                   attack=0.002, curve=4.5)
    sparks = envelope(highpass(noise(0.3, 30), 2600), attack=0.002, curve=6.0)
    return mix(gain(hit, 0.85), gain(sparks, 0.45))


EFFECTS = {
    "shoot": (shoot, 0.62),
    "lob": (lob, 0.60),
    "splat": (splat, 0.78),
    "explode": (explode, 0.92),
    "plant": (plant, 0.72),
    "shovel": (shovel, 0.60),
    "gulp": (gulp, 0.62),
    "sun": (sun, 0.68),
    "mower": (mower, 0.50),
    "click": (click, 0.42),
    "bite": (bite, 0.80),
    "zombie-dies": (zombie_dies, 0.62),
    "armour-breaks": (armour_breaks, 0.80),
    "plant-dies": (plant_dies, 0.64),
    "grave-breaks": (grave_breaks, 0.80),
    "rise": (rise, 0.56),
    "storm": (storm, 0.46),
    "tide": (tide, 0.44),
    "mint": (mint, 0.62),
    "wave": (wave_call, 0.66),
    "huge-wave": (huge_wave, 0.72),
    "boss-move": (boss_move, 0.70),
    "boss-hurt": (boss_hurt, 0.74),
}


def main():
    os.makedirs(OUT, exist_ok=True)
    total = 0
    for name in sorted(EFFECTS):
        build, peak = EFFECTS[name]
        path, size = save(name, build(), peak)
        total += size
        print("%-16s %7d bytes  peak %.2f" % (name, size, peak))
    print("%d effects, %.0f KB" % (len(EFFECTS), total / 1024.0))


if __name__ == "__main__":
    main()
