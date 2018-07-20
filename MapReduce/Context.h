
//Context.h


#ifndef MAPREDUCE_CONTEXT
#define MAPREDUCE_CONTEXT 

#include "common.h"

namespace mapreduce {

    using std::list;
    using std::string;

    template<class K,class V>
    class Context{
        public:
            list<K> keys;
            list<V> values;
            Context(){};
            virtual ~Context(){};
            virtual int clear(){keys.clear();values.clear();return 1;}
            virtual int write(const K& key,const V& value){keys.push_back(key);values.push_back(value);return 1;}
    };

    template<class K, class V>
    int serializeKVpair(const K &key, const V &value, string &result) {
        std::stringstream ss;
        ss << key << " " << value;
        result = ss.str();
        return 1;
    }

    // deserialize a kv pair from SOURCE,
    // store the result in two reference.
    template<class K, class V>
    int deserializeKVpair(K &key, V &value, string &source) {
        std::stringstream ss(source);
        ss >> key >> value;
        return 1;
    };

    // Format of serialization: key L1 L2 L3 ... Ln (separated by space)
    template<class K, class V>
    int serializeKListPair(const K &key, const list<V> &L, string &result) {
        std::stringstream ss;
        ss << key << " ";
        for(auto e:L) {
            ss << e << " ";
        }
        result = ss.str();
        return 1;
    };

    template<class K, class V>
    int deserializeKListPair(K &key, list<V> &L, string &source) {
        std::stringstream ss(source);
        ss >> key;
        V v;
        while(ss >> v) {
            L.push_back(v);
        }
        return 1;
    };

}


#endif //MAPREDUCE_CONTEXT