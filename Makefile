
PEOPLE_DAT=people.dat

JAVAFILES=Photoboard.java
MAINCLASS=Photoboard

CLASSPATH=lib/iText.jar

OUTDIR=output

CLASSFILES=$(patsubst %.java, $(OUTDIR)/%.class, $(JAVAFILES))

###############################################################################

all: $(OUTDIR)/poster.pdf # $(OUTDIR)/poster_tiled.pdf

build: $(CLASSFILES)

$(OUTDIR)/%.class: %.java
	@mkdir -p $(OUTDIR)
	javac -cp $(CLASSPATH) -d $(OUTDIR) $^
	
$(OUTDIR)/poster.pdf: build
	java -cp $(CLASSPATH):$(OUTDIR) $(MAINCLASS) $(PEOPLE_DAT) $@

# TODO: fix this one
$(OUTDIR)/poster_tiled.pdf: build
	convert poster.pdf -crop 3x2@ +repage +adjoin tile_3x3@_%d.pdf
	# Merge into single PDF
	pdftk tile_* cat output tile.pdf

clean:
	rm -rf $(OUTDIR)
