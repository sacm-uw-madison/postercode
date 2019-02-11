# Postercode

This is the code to generate the annual photoboard.

To build the program and run it: `make`. This will output a file
`output/poster.pdf`, which is the poster.

You will need to provide:
- `people.dat`: A file where each person has a line with the format:
  `user:first:last:office`, where `user` is the person's username.
- `pics/` a directory with all the pictures. The picture for user `user` should
  be named `pics/user.jpg`.

The `stampPerson` method in `Photoboard.java` is the method to modify if you
want to change the format of each person's photo/caption.

The `assets/` directory contains
- The font used for the captions
- The background texture image
- The unknown-person image
- The title image

There are some old photo collections in the `~sacmuse` directory on AFS. You
can use these to test the script. To change the location of the `pics/`
directory, set the `PICS_DIRECTORY` variable at the top of the `Makefile`.

### Printing the Poster

You can print the poster for about $40 at Hellen C. White College library. You
will need to convert the `poster.pdf` to a TIFF file, since that is what they
require. It usually takes a couple of hours to print.

When converting the PDF to TIFF, you can use `ImageMagik`. You will need to set
the resolution to the highest possible. The file size will be rather large, so
be prepared with a thumb drive to take to the library.
