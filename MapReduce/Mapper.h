
// Mapper.h

#ifndef MAPREDUCE_MAPPER
#define MAPREDUCE_MAPPER 

#include "common.h"
#include "InputFormat.h"
#include "Shuffle.h"
namespace mapreduce {
using std::list;

// abstract class of Mapper
// when initialize a mapper object,the object get a split of the file
// run() will convert the input to (key,value) format, and call map()
// for every (k,v), then sort and merge the result.

template<class K1,class V1,class K2,class V2,class Format = class TextFormat>
class Mapper {
 public:
  string& input;
  list<K1> keys;
  list<V1> values;
  Context<K2,V2> context;
  Mapper(string& split) : input(split){}
  virtual ~Mapper(){keys.clear();values.clear();context.clear();};

  virtual int map(const K1& key,const V1& value) = 0;

  int run(list<K2>& key_res,list<list<V2>>& value_res){
    // for reference only 
    Format format(input);
    ASSERT(format.formatting(keys,values));
    // print_pair<K1,V1>(keys,values);
    
    while(!keys.empty() && !values.empty()){
      ASSERT(map(keys.front(),values.front()));
      keys.pop_front();
      values.pop_front();
    }
    //print_pair<K2,V2>(context.keys,context.values);
    
    ASSERT((mapreduce::sort<K2,V2>(context.keys,context.values)));
    // print_pair<K2,V2>(context.keys,context.values);
    ASSERT((mapreduce::merge_value<K2,V2>(context.keys,context.values,key_res,value_res)));
    // print_pair_list<K2,V2>(key_res,value_res);
    return 1;
  }

};
}

//register a mapper written by user
#define REGISTER_MAPPER(M,K1,V1,K2,V2)\
int doMapper(string& input,string& result,string& errorMessage,int partition_index[]) {\
    Context<K2,list<V2> > context;\
    M mapper(input);\
    ADDERROR((mapper.run(context.keys,context.values)));\
    ADDERROR((mapreduce::partition<K2,V2,BKDRHash<K2>,5>(context,result,partition_index)));\
    return 1;\
} 

#endif //MAPREDUCE_MAPPER