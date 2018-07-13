
#ifndef MAPREDUCE_INPUTFORMAT
#define MAPREDUCE_INPUTFORMAT 

#include <string>
#include <list>
namespace mapreduce {

using std::string;
using std::list;

class InputFormat {
 public:
  string& input;
  InputFormat(string& split):input(split){}
  virtual ~InputFormat();
  virtual int formatting() = 0;
  
};

class TextFormat : public InputFormat {
 public:
  virtual int formatting(list<int>& keys,list<string>& values);
};

template<class K,class V>
class KeyValueFormat : public InputFormat {
 public:
  virtual int formatting(list<K>& keys,list<V>& values);
};
}
#endif