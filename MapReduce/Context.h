
//Context.h


#ifndef MAPREDUCE_CONTEXT
#define MAPREDUCE_CONTEXT 

#include "common.h"

namespace mapreduce {

using std::list;
using std::string;
template<class K,class V>
class Context {
 public:
  list<K> keys;
  list<V> values;

  Context();
  virtual ~Context();

  virtual int clear();
  //write() method should add the new key and value into their lists
  //map() and reduce() may call the method Context.write() to record their outputs
  virtual int write(const K& key,const V& value);

  //serialize two lists into a string
  //check the size first
  virtual int serialize(string& result);

  //deserialize two lists
  virtual int deserialize(string& result);

  virtual int serialize_list(string& result);
  virtual int deserialize_list(string& result);
};


    template<class K, class V>
    int Context<K, V>::deserialize_list(string &result) {
        std::stringstream ss(result);
        std::string token;
        while(std::getline(ss, token, '\n')) {
            std::stringstream line(token);
            while()
        }
        return 0;
    }

    template<class K, class V>
    Context<K, V>::~Context() {
        return;
    }

    /***
     * The format for serializing key-list pair
     * is: key1 l1 l2 l3 ... ln \n key2 l1 l2 ....
     * @tparam K
     * @tparam V
     * @param result
     * @return
     */
    template<class K, class V>
    int Context<K, V>::serialize_list(string &result) {
        std::stringstream ss;
        auto itKey = keys.begin();
        auto itValue = values.begin();
        for(; itKey != keys.end() && itValue !=values.end(); ++itKey, ++itValue)
        {
            ss << *itKey << " ";
            for(auto element:*itValue) {
                ss << element << " ";
            }
            ss << '\n';
        }
        result = ss.str();
        return 0;
    }



    template<class K, class V>
    int Context<K, V>::write(const K &key, const V &value) {
        keys.push_back(key);
        values.push_back(value);
        return 0;
    }

    /**
     *
     * @tparam K
     * @tparam V
     * @param result: k,v seperated by space,
     *                "k1 v1 k2 v2 k3 v3"
     *                kv pair seperated by space
     * @return 1 if success else -1
     */
    template<class K, class V>
    int Context<K, V>::serialize(string &result) {
        std::stringstream ss;
        auto itKey = keys.begin();
        auto itValue = values.begin();
        for(; itKey != keys.end() && itValue !=values.end(); ++itKey, ++itValue)
        {
            ss << *itKey << " " << *itValue <<" ";
        }
        result = ss.str();
        return 0;
    }


    template<class K, class V>
    int Context<K, V>::deserialize(string &result) {
        std::stringstream ss(result);
        K k;
        V v;
        while(ss >> k >> v) {
            write(k, v);
        }
        return 0;
    }

    template<class K, class V>
    int Context<K, V>::clear() {
        keys.clear();
        values.clear();
        return 0;
    }

    template<class K, class V>
    Context<K, V>::Context() {
        return;
    }
}

#endif //MAPREDUCE_CONTEXT