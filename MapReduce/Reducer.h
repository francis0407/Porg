#ifndef MAPREDUCE_REDUCER
#define MAPREDUCE_REDUCER 
#include "Context.h"
#include <list>
namespace mapreduce {

using std::list;
using std::string;
template<class K1,class V1,class K2,class V2>
class Reducer {
 public：
  list<K1>& keys;
  list<list<V1>>& values;
  Context<K2,V2> context;
  Reducer(list<K1>& k,list<list<V1>>& v):keys(k),values(v);
  virtual ~Reducer();
  virtual int reduce(const K1& key,const list<V1>& value) = 0;
  virtual int run(){
    //for reference only
    while(!keys.empty() && !values.empty()){
      ASSERT(reduce(keys.front(),values.front()));
      keys.pop_front();
      values.pop_front();
    }
  } 
  
};

}
#define REGISTER_REDUCER(R,K1,V1,K2,V2)\
int doReducer(list<string>& input,string& result,string& errorMessage){\
  int len = input.size();\
  Context<K1,list<V1>> temp,target;\
  ADDERROR(len);\
  ADDERROR(target.deserialize(input.front()));\
  input.pop_front();\
  while(!input.empty()){\
    ADDERROR(temp.deserialize(input.front()));\
    while(!temp.keys.empty()){\
      ADDERROR(target.keys.push_back(temp.keys.front()));\
      ADDERROR(target.values.push_back(temp.values.front()));\
      ADDERROR(temp.keys.pop_front());\
      ADDERROR(temp.values.pop_front());\
    }\
    ADDERROR(input.pop_front());\
    ADDERROR(temp.clear());\
  }\
  ADDERROR(mapreduce::sort<K1,list<V1>>(target.keys,target.values));\
  ADDERROR(mapreduce::merge_list<K1,V1>(target.keys,target.values,temp.keys,temp.values));\
  ADDERROR(target.clear());\
  R reducer(temp.keys,temp.values);\
  ADDERROR(reducer.run());\
  ADDERROR(reducer.context.serialize(result));\
  return 1;\
}

#endif //MAPREDUCE_REDUCER