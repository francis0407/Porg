
// Mapper.h

#ifndef MAPREDUCE_MAPPER
#define MAPREDUCE_MAPPER

#include <emscripten.h>
#include <iostream>
#include <cstring>
#include "common.h"
#include "InputFormat.h"
#include "Shuffle.h"

namespace mapreduce {
  int doMapper(std::string &input, std::string &result, std::string &errorMessage, int partition_index[]);
}

char *__wasmMapperResultMemory = nullptr;
char *__wasmMapperErrorMessageMemory = nullptr;
int __wasmMapperPartitionIndexMemory[REDUCER_NUM] = { 0 };

//register a mapper written by user
#define REGISTER_MAPPER(M, K1, V1, K2, V2)                                                                 \
  int doMapper(string &input, string &result, string &errorMessage, int partition_index[])                 \
  {                                                                                                        \
    Context<K2, list<V2>> context;                                                                         \
    M mapper(input);                                                                                       \
    ADDERROR((mapper.run(context.keys, context.values)));                                                  \
    ADDERROR((mapreduce::partition<K2, V2, HASH_ALG<K2>, REDUCER_NUM>(context, result, partition_index))); \
    return 1;                                                                                              \
  }

int _doMapperWrapper_(const char* input) {
  std::string i(input), r, e;
  if (__wasmMapperResultMemory)
    delete[] __wasmMapperResultMemory;
  if (__wasmMapperErrorMessageMemory)
    delete[] __wasmMapperErrorMessageMemory;
  int ret = mapreduce::doMapper(i, r, e, __wasmMapperPartitionIndexMemory);
  __wasmMapperResultMemory = new char[r.size() + 1];
  __wasmMapperErrorMessageMemory = new char[e.size() + 1];
  memcpy(__wasmMapperResultMemory, r.c_str(), r.size() + 1);
  memcpy(__wasmMapperErrorMessageMemory, e.c_str(), e.size() + 1);
  return ret;
}

// wasm exports
extern "C" {
char* EMSCRIPTEN_KEEPALIVE __getMapperResultRef() {
  return __wasmMapperResultMemory;
}

char* EMSCRIPTEN_KEEPALIVE __getMapperErrorMessageRef() {
  return __wasmMapperErrorMessageMemory;
}

int* EMSCRIPTEN_KEEPALIVE __getMapperPartitionRef() {
  return __wasmMapperPartitionIndexMemory;
}

int EMSCRIPTEN_KEEPALIVE __getPartitionIndexLength() {
  return REDUCER_NUM;
}

int EMSCRIPTEN_KEEPALIVE __doMapperWrapper__(const char* input) {
  return _doMapperWrapper_(input);
}
}

namespace mapreduce
{
using std::list;

// abstract class of Mapper
// when initialize a mapper object,the object get a split of the file
// run() will convert the input to (key,value) format, and call map()
// for every (k,v), then sort and merge the result.

template <class K1, class V1, class K2, class V2, class Format = class TextFormat>
class Mapper
{
public:
  string &input;
  list<K1> keys;
  list<V1> values;
  Context<K2, V2> context;
  Mapper(string &split) : input(split) {}
  virtual ~Mapper()
  {
    keys.clear();
    values.clear();
    context.clear();
  };

  virtual int map(const K1 &key, const V1 &value) = 0;

  int run(list<K2> &key_res, list<list<V2>> &value_res)
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
};
} // namespace mapreduce

#endif //MAPREDUCE_MAPPER
