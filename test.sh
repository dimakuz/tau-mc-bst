#!/bin/bash
./compile
for T in 1 2 4 16 32 128
do
	for K in 256 2560 25600 256000 2560000 25600000 256000000
	do
		./run $T 10 10 -keys$K -ins40 -del40 -prefill
		./run $T 10 10 -keys$K -ins40 -del40
	done
done
