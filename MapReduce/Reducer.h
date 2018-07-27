
//Reducer.h

#ifndef MAPREDUCE_REDUCER
#define MAPREDUCE_REDUCER
#include <emscripten.h>
#include <cstring>
#include "Context.h"
#include "common.h"

namespace mapreduce {
  using std::string;
  using std::list;
  int doReducer(list<string> &input, string &result, string &errorMessage);
}

#define REGISTER_REDUCER(R, K1, V1, K2, V2)                                                                        \
  int doReducer(list<string> &input, string &result, string &errorMessage)                                         \
  {                                                                                                                \
    Context<K1, list<V1>> temp, target;                                                                            \
    for (auto iter = input.begin(); iter != input.end(); iter++)                                                   \
    {                                                                                                              \
      int start = 0;                                                                                               \
      for (int i = 0; i < iter->size(); i++)                                                                       \
        if ((*iter)[i] == '\t')                                                                                    \
        {                                                                                                          \
          string s = iter->substr(start, i - start);                                                               \
          K1 key;                                                                                                  \
          list<V1> value;                                                                                          \
          ADDERROR((deserializeKListPair(key, value, s)));                                                         \
          temp.keys.push_back(key);                                                                                \
          temp.values.push_back(value);                                                                            \
          start = i + 1;                                                                                           \
        }                                                                                                          \
    }                                                                                                              \
    ADDERROR((mapreduce::sort<K1, list<V1>>(temp.keys, temp.values)));                                             \
    ADDERROR((mapreduce::merge_list<K1, V1>(temp.keys, temp.values, target.keys, target.values)));                 \
    R reducer(target.keys, target.values);                                                                         \
    ADDERROR((reducer.run()));                                                                                     \
    auto iter_k = reducer.context.keys.begin();                                                                    \
    for (auto iter_v = reducer.context.values.begin(); iter_v != reducer.context.values.end(); iter_v++, iter_k++) \
    {                                                                                                              \
      string buf;                                                                                                  \
      ADDERROR((mapreduce::serializeKVpair<K2, V2>(*iter_k, *iter_v, buf)));                                       \
      buf.push_back('\n');                                                                                         \
      result += buf;                                                                                               \
    }                                                                                                              \
    return 1;                                                                                                      \
  }

char *__wasmReducerResultMemory = nullptr;
char *__wasmReducerErrorMessageMemory = nullptr;
std::list<std::string> __wasmReducerInputList;

void __pushStringToInputList(char *str) {
  __wasmReducerInputList.push_front(std::string(str));
}

int _doReducerWrapper_() {
  std::string result, error;
  int ret = mapreduce::doReducer(__wasmReducerInputList, result, error);
  __wasmReducerInputList.clear();
  if (__wasmReducerResultMemory)
    delete[] __wasmReducerResultMemory;
  if (__wasmReducerErrorMessageMemory)
    delete[] __wasmReducerErrorMessageMemory;
  __wasmReducerResultMemory = new char[result.size() + 1];
  __wasmReducerErrorMessageMemory = new char[error.size() + 1];
  memcpy(__wasmReducerResultMemory, result.c_str(), result.size() + 1);
  memcpy(__wasmReducerErrorMessageMemory, error.c_str(), error.size() + 1);
  return ret;
}

extern "C" {
void EMSCRIPTEN_KEEPALIVE __addStringToInputList(char *str) {
  __pushStringToInputList(str);
}

int EMSCRIPTEN_KEEPALIVE __doReducerWrapper__() {
  return _doReducerWrapper_();
}

char* EMSCRIPTEN_KEEPALIVE __getReducerResultRef() {
  return __wasmReducerResultMemory;
}

char* EMSCRIPTEN_KEEPALIVE __getReducerErrorRef() {
  return __wasmReducerErrorMessageMemory;
}
}

namespace mapreduce
{

using std::list;
using std::string;
template <class K1, class V1, class K2, class V2>
class Reducer
{
public:
  list<K1> &keys;
  list<list<V1>> &values;
  Context<K2, V2> context;
  Reducer(list<K1> &k, list<list<V1>> &v) : keys(k), values(v){};
  virtual ~Reducer(){};
  virtual int reduce(const K1 &key, const list<V1> &value) = 0;
  int run()
  {
    //for reference only
    while (!keys.empty() && !values.empty())
    {
      ASSERT((reduce(keys.front(), values.front())));
      keys.pop_front();
      values.pop_front();
    }
    return 1;
  }
};
} // namespace mapreduce

#endif //MAPREDUCE_REDUCER
