# MapReduce 

## Local Test

### Compile source files
> g++ -std=c++11 WordCount.cpp main_map.cpp -o map
> g++ -std=c++11 WordCount.cpp main_reduce.cpp -o reduce

### Run WordCount Test
> ./map [input files...]
> ./reduce [input files...] [output file] 