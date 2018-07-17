
//Context.h


#ifndef MAPREDUCE_CONTEXT
#define MAPREDUCE_CONTEXT 

#include "common.h"

namespace mapreduce {

using std::list;
using std::string
template<class K,class V>
class Context {
 public:
  list<K> keys;
  list<V> values;
  Context();
  virtual ~Context();

  //write() method should add the new key and value into their lists
  //map() and reduce() may call the method Context.write() to record their outputs
  virtual int write(const K& key,const V& value);
  
  //serialize two lists into a string
  //check the size first
  virtual int serialize(string& result);

  //deserialize two lists
  virtual int deserialize(string& input);
};
}

#endif //MAPREDUCE_CONTEXT