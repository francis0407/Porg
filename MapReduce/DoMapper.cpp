
#include "Mapper.h"
#include "InputFormat.h"
#include "Context.h"
#include "Shuffle.h"
#include <string>

namespace mapreduce{

using std::string;

int doMapper(string& input) {
    string result;
    int partition_index[REDUCER_NUM];
    TestMapper mapper(input);
    mapper.run();
    mapreduce::partition<int,string,Hash>(mapper.context,result,partition_index);
} 
}