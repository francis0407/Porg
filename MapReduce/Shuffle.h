#ifndef MAPREDUCE_SHUFFLE
#define MAPREDUCE_SHUFFLE

#include <list>
#include <utility>
#include <string>
#include <iostream>
#include "Context.h"
#include "HashAlgorithm.h"
#include "common.h"

namespace mapreduce {

using std::list;
using std::pair;
using std::make_pair;
using std::string;

// sort by keys
template<class K, class V>
int sort(list<K>& keys,list<V>& values) {
	list<pair<K, V>> joint;
	pair<K, V> tmp;
	if (keys.size() != values.size()) {
		string msg("ERR mapreduce::sort: Number of keys and values not equal.");
		// ErrorMessage::append(msg);
		return 0;
	}
	while (!keys.empty()) {
		tmp = make_pair(keys.front(), values.front());
		joint.push_back(tmp);
		keys.pop_front();
		values.pop_front();
	}
	joint.sort();
	while (!joint.empty()) {
		tmp = joint.front();
		keys.push_back(tmp.first);
		values.push_back(tmp.second);
		joint.pop_front();
	}
	return 1;
}

//merge <K,V> to <K,list<V>>, merge by keys
template<class K, class V>
int merge_value(list<K>& keys, list<V>& values, list<K>& des_keys, list< list<V> >& des_values) {
	K k;
	list<V> v;
	unsigned long long cnt;
	des_keys.clear();
	des_values.clear();
	if (keys.size() != values.size() || keys.size() == 0 || values.size() == 0) {
		string msg("ERR mapreduce::merge_value: Invalid number of keys or values.");
		// ErrorMessage::append(msg);
		return 0;
	}
	while (!keys.empty()) {
		k = keys.front();
		des_keys.push_back(k);
		keys.pop_front();
		v.clear();
		cnt = 1;
		while (!keys.empty() && !(keys.front() < k || k < keys.front())) {
			++cnt;
			keys.pop_front();
		}
		while (cnt--) {
			v.push_back(values.front());
			values.pop_front();
		}
		des_values.push_back(v);
	}
	return 1;
}

//merge <K,list<V>> to <K,list<V>>
template<class K,class V>
int merge_list(list<K>& keys, list< list<V> >& values, list<K>& des_keys, list< list<V> >& des_values) {
	K k;
	list<V> v;
	unsigned long long cnt;
	des_keys.clear();
	des_values.clear();
	if (keys.size() != values.size() || keys.size() == 0 || values.size() == 0) {
		string msg("ERR mapreduce::merge_list: Invalid number of keys or values.");
		// ErrorMessage::append(msg);
		return 0;
	}
	while (!keys.empty()) {
		k = keys.front();
		des_keys.push_back(k);
		keys.pop_front();
		v.clear();
		cnt = 1;
		while (!keys.empty() && !(keys.front() < k || k < keys.front())) {
			++cnt;
			keys.pop_front();
		}
		while (cnt--) {
			v.merge(values.front());
			values.pop_front();
		}
		des_values.push_back(v);
	}
	return 1;
}


template<class K, class V, class Hash, int s>
int partition(Context<K,list<V> >& context, string& result, int index[]) {
	list< pair<K, list<V> > > buffer[s];
	Hash hash(s);
	string str, buf;
	auto j = context.values.begin();
	for (auto i = context.keys.begin(); i != context.keys.end(); ++i, ++j){
		buffer[hash.hash(*i)].push_back(make_pair(*i, *j));
	}
	result.clear();
	for (int i = 0; i < s; ++i) {
		buf.clear();
		for (auto t = buffer[i].begin(); t != buffer[i].end(); ++t) {
			auto k = (*t).first;
			auto v = (*t).second;
			serializeKListPair<K,V>(k, v, str);
			buf += str + '\t';
		}
		index[i] = result.length();
		result += buf;
	}
	return 1;
}

}
#endif
