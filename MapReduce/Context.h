
//Context.h

#ifndef MAPREDUCE_CONTEXT
#define MAPREDUCE_CONTEXT

#include "common.h"

namespace mapreduce
{
using std::list;
using std::string;

template <class K, class V>
class Context
{
  public:
    list<K> keys;
    list<V> values;
    Context(){};
    virtual ~Context(){};
    int clear();
    int write(const K &key, const V &value);
};
template <class K, class V>
int serializeKVpair(const K &key, const V &value, string &result);

// deserialize a kv pair from SOURCE,
// store the result in two reference.
template <class K, class V>
int deserializeKVpair(K &key, V &value, string &source);

// Format of serialization: key L1 L2 L3 ... Ln (separated by space)
template <class K, class V>
int serializeKListPair(const K &key, const list<V> &L, string &result);

template <class K, class V>
int deserializeKListPair(K &key, list<V> &L, string &source);
} // namespace mapreduce

#endif //MAPREDUCE_CONTEXT