# Postercode

This is the code to generate the annual photoboard.

To build the program and run it: `make`. This will output a file
`output/poster.pdf`, which is the poster.

You will need to provide:
- `people.dat`: A file where each person has a line with the format:
  `user:first:last:office`, where `user` is the person's username.
- `pics/` a directory with all the pictures. The picture for user `user` should
  be named `pics/users.jpg`.
