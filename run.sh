rm frames/*
javac src/QuadTree.java src/Picture.java
java -cp src/ QuadTree $1 $2
ffmpeg -framerate 15 -i frames/frameno%d.png  -vcodec libx264 -pix_fmt yuv420p -crf 25  output.mp4