#ifndef MAPREDUCE_CONTEXT
#define MAPREDUCE_CONTEXT 
#include <list>
#include <string>
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
  virtual int write(K& key,V& value);
  virtual int serialize(string& result);
  virtual int deserialize(string& input);
};
}

#endif