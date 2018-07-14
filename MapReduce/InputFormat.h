
// InputFormat.h

#ifndef MAPREDUCE_INPUTFORMAT
#define MAPREDUCE_INPUTFORMAT 

#include "common.h"

namespace mapreduce {

using std::string;
using std::list;

//Base class for input formats
//Only support file input
class InputFormat {
 public:
  string& input;
  InputFormat(string& split):input(split){}
  virtual ~InputFormat();
  virtual int formatting() = 0;
  
};

//Default input format
//(k_i,v_i) = (the offset of the first character in line i,the text(string) of line i)
class TextFormat : public InputFormat {
 public:
  virtual int formatting(list<int>& keys,list<string>& values);
};

//KeyValue input format
//the file must using separator to separate key and value in a line
template<class K,class V>
class KeyValueFormat : public InputFormat {
 public:
  const char SEPARATOR = '\t';
  virtual int formatting(list<K>& keys,list<V>& values);
};
}
#endif //MAPREDUCE_INPUTFORMAT