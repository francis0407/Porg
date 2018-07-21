
//Reducer.cpp

#include "Reducer.h"

namespace mapreduce
{

template <class K1, class V1, class K2, class V2>
int Reducer<K1, V1, K2, V2>::run()
{
    while (!keys.empty() && !values.empty())
    {
        ASSERT((reduce(keys.front(), values.front())));
        keys.pop_front();
        values.pop_front();
    }
    return 1;
}
} // namespace mapreduce