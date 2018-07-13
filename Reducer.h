#ifndef MAPREDUCE_REDUCER
#define MAPREDUCE_REDUCER 
#include "Context.h"
#include <list>
namespace mapreduce {

using std::list;

template<class K1,class V1,class K2,class V2>
class Reducer {
 public：
  list<K1> keys;
  list<V1> values;
  Context<K2,V2> context;
  virtual ~Reducer();
  virtual int reduce(K1 key,List<V1> value) = 0;
  
};
}
#endif