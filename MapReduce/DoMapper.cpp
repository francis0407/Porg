
#include "Mapper.h"
#include "InputFormat.h"
#include "Context.h"
#include "Shuffle.h"
#include "common.h"


namespace mapreduce{

using std::string;

int doMapper(string& input,string& result,int partition_index[]) {
    // for reference only

    Context<int,list<string>> context;
    TestMapper mapper(input);
    //context will record <k,list<v>>
    mapper.run(context.keys,context.values);

    //partition and  serialization
    mapreduce::partition<int,string,Hash>(context,result,partition_index);
    return 1;
} 
}