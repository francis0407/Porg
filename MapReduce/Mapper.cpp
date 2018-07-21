
//Mapper.cpp

#include "Mapper.h"

namespace mapreduce
{
    
template <class K1, class V1, class K2, class V2, class Format = class TextFormat>
virtual Mapper<K1, V1, K2, V2, Format>::~Mapper()
{
    keys.clear();
    values.clear();
    context.clear();
};

template <class K1, class V1, class K2, class V2, class Format = class TextFormat>
int Mapper<K1, V1, K2, V2, Format>::run(list<K2> &key_res, list<list<V2>> &value_res)
{
    // for reference only
    Format format(input);
    ASSERT(format.formatting(keys, values));
    // print_pair<K1,V1>(keys,values);

    while (!keys.empty() && !values.empty())
    {
        ASSERT(map(keys.front(), values.front()));
        keys.pop_front();
        values.pop_front();
    }
    //print_pair<K2,V2>(context.keys,context.values);

    ASSERT((mapreduce::sort<K2, V2>(context.keys, context.values)));
    // print_pair<K2,V2>(context.keys,context.values);
    ASSERT((mapreduce::merge_value<K2, V2>(context.keys, context.values, key_res, value_res)));
    // print_pair_list<K2,V2>(key_res,value_res);
    return 1;
}
} // namespace mapreduce