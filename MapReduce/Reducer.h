#ifndef MAPREDUCE_REDUCER
#define MAPREDUCE_REDUCER 
#include "Context.h"
#include "common.h"
namespace mapreduce {

using std::list;
using std::string;
template<class K1,class V1,class K2,class V2>
class Reducer {
 public:
  list<K1>& keys;
  list< list<V1> >& values;
  Context<K2,V2> context;
  Reducer(list<K1>& k,list< list<V1> >& v):keys(k),values(v){};
  virtual ~Reducer(){};
  virtual int reduce(const K1& key,const list<V1>& value) = 0;
  int run(){
    //for reference only
    while(!keys.empty() && !values.empty()){
      ASSERT((reduce(keys.front(),values.front())));
      keys.pop_front();
      values.pop_front();
    }
    return 1;
  } 
  
};
}
#define REGISTER_REDUCER(R,K1,V1,K2,V2)\
int doReducer(list<string>& input,string& result,string& errorMessage){\
  Context<K1,list<V1>> temp,target;\
  for(auto iter = input.begin();iter != input.end();iter++){\
    int start = 0;\
    for(int i = 0;i < iter->size();i++ )\
      if((*iter)[i] == '\t' ){\
        string s = iter->substr(start,i-start);\
        K1 key;\
        list<V1> value;\
        ADDERROR((deserializeKListPair(key,value,s)));\
        temp.keys.push_back(key);\
        temp.values.push_back(value);\
        start = i+1;\
      }\
  }\
  ADDERROR((mapreduce::sort<K1,list<V1> >(temp.keys,temp.values)));\
  ADDERROR((mapreduce::merge_list<K1,V1>(temp.keys,temp.values,target.keys,target.values)));\
  R reducer(target.keys,target.values);\
  ADDERROR((reducer.run()));\
  auto iter_k = reducer.context.keys.begin();\
  for(auto iter_v = reducer.context.values.begin();iter_v!=reducer.context.values.end();iter_v++,iter_k++){\
    string buf;\
    ADDERROR((mapreduce::serializeKVpair<K2,V2>(*iter_k,*iter_v,buf)));\
    buf.push_back('\n');\
    result += buf;\
  }\
  return 1;\
}

#endif //MAPREDUCE_REDUCER