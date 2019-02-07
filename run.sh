# Compile
javac -cp . photoboard.java
# Run
java -cp . photoboard
# Break into smaller pdf for printing:
#convert poster.pdf -crop 1225x1650 +repage tile-%d.pdf
convert poster.pdf -crop 3x2@ +repage +adjoin tile_3x3@_%d.pdf
# Merge into single PDF
pdftk tile_* cat output tile.pdf

