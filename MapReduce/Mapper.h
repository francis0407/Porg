
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
  virtual ~Mapper();

  virtual int map(K1& key,V1& value) = 0;

  virtual int run(list<K2>& key_res,list<list<V2>>& value_res){
    // for reference only 
    Format format(input);
    int len = format.formatting(keys,values);
    while(!keys.empty() && !values.empty()){
      map(keys.front(),values.front());
      keys.pop_front();
      values.pop_front();
    }
    
    mapreduce::sort<K2,V2>(context.keys,context.values);
    mapreduce::merge_value<K2,V2>(context.keys,context.values,key_res,value_res);

  }

};
}
#endif