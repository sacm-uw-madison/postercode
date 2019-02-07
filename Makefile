
JAVAFILES=photoboard.java
MAINCLASS=photoboard

CLASSPATH=lib/iText.jar

OUTDIR=output/

CLASSFILES=$(patsubst %.java, $(OUTDIR)/%.class, $(JAVAFILES))

build: $(CLASSFILES)

$(OUTDIR)/%.class: %.java
	@mkdir -p $(OUTDIR)
	javac -cp $(CLASSPATH) -d $(OUTDIR) $^
	
poster.pdf: build
	java -cp $(CLASSPATH):$(OUTDIR) $(MAINCLASS)

poster_tiled.pdf: build
	convert poster.pdf -crop 3x2@ +repage +adjoin tile_3x3@_%d.pdf
	# Merge into single PDF
	pdftk tile_* cat output tile.pdf
