#!/bin/bash -xe
./compile
for K in 256 256000 256000000
do
	for INS in 10 35 40
	do
		export DEL=$(( 80 - $INS ))
		for T in 1 4 32 128
		do
			./run $T 4 10 -keys$K -ins$INS -del$DEL -seed$RANDOM
		done
	done
done
