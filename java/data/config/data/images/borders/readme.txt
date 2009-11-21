This folder contains border textures, that can be used in a Widget's code.

Each border texture image must contain information about the width of the four borders. These information are read from the center pixels' read and green channels as follows.

Make sure, your texture image has an area in the center, that is not effectively used when rendering. So if you want to use 13 pixels for the left and right borders, and 15 pixels for the top and bottom borders, your texture must be at least of the following size:
13 + 2 + 13 x 15 + 2 + 15 = 28 x 32

The width of the left border is read from the following pixel:
floor(width / 2) x floor(height / 2)

The height of the top border is read from the floowing pixel:
(floor(width / 2) + 1) x floor(height / 2)

The width of the right border is read from the floowing pixel:
floor(width / 2) x (floor(height / 2) + 1)

The height of the bottom border is read from the floowing pixel:
(floor(width / 2) + 1) x (floor(height / 2) + 1)

The red channel's color value defines the width used of the image data and the green channel defines the visible width (i.e the width without a transparent part).
