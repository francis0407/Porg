#ifndef MAPREDUCE_SHUFFLE
#define MAPREDUCE_SHUFFLE

#include <list>

namespace mapreduce {

using std::list;

template<class K,class V>
int sort(list<K>& keys,list<V>& values);

//merge <K,V> to <K,list<V>>
template<class K,class V>
int merge_value(list<K>& keys,list<V>& values,list<K>& des_keys,list<list<V>>& des_values);

//merge <K,list<V>> to <K,list<V>>
template<class K,class V>
int merge_list(list<K>& keys,list<list<V>>& values,list<K>& des_keys,list<list<V>>& des_values);

template<class K,class V,class Hash>
int partition(Context<K,V>& context,string& result,int index[]);
}

#endif