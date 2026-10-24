"""Clip segments for each pack (from scripts/cuts.py, rapid-cut runs merged). Shared by the catalog."""
CAT_CUTS = [0, 6.73, 13.93, 19.83, 24.67, 28.53, 38.37, 43.67, 53.87, 58.87, 63.73, 70.00, 74.43, 79.37,
            83.17, 92.43, 95.77, 103.87, 110.70, 118.53, 124.10, 132.10, 136.97, 152.60, 155.60, 162.03,
            168.50, 175.77, 181.20, 194.63]
DOG_CUTS = [0, 6.43, 14.50, 21.47, 32.10, 38.60, 43.67, 51.43, 61.00, 67.83, 75.57, 83.07, 86.57, 97.33,
            104.20, 111.00, 117.20, 123.30, 129.73, 136.50, 139.17, 145.63, 155.43, 158.73, 162.00, 167.57,
            175.13, 179.13, 185.57, 191.37]


def segments(cuts):
    return [(a, b) for a, b in zip(cuts, cuts[1:]) if b - a >= 0.9]


# Every pack in the catalog, in numbering order: (prefix, file, cut list or explicit segments).
# Single-meme videos added later continue the cat (C) / dog (D) numbering.
PACKS = [
    ("C", "assets/packs/cat_pack.mp4", segments(CAT_CUTS)),
    ("C", "assets/packs/cat_german_painter.mp4", [(0.0, 4.9)]),
    ("C", "assets/packs/cat_screaming_orange.mp4", [(0.6, 3.5)]),  # 0-0.6 is a different cat + transition
    ("C", "assets/packs/cat_happy_happy.mp4", [(0.0, 7.1)]),
    # banana-suit cat pack: 0-2.07 skipped (big Chinese title over the picture); 36.2-46.4 = rapid spins
    ("C", "assets/packs/cat_banana_pack.mp4", [(2.07, 5.10), (5.10, 10.30), (10.30, 20.07), (20.07, 25.23),
                                               (25.23, 30.43), (30.43, 36.20), (36.20, 46.37), (46.37, 52.43),
                                               (52.43, 56.57), (56.57, 61.77), (61.77, 71.30)]),
    ("D", "assets/packs/dog_pack.mp4", segments(DOG_CUTS)),
]
