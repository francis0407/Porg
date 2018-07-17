
// InputFormat.h

#ifndef MAPREDUCE_INPUTFORMAT
#define MAPREDUCE_INPUTFORMAT

#include "common.h"

namespace mapreduce
{

using std::list;
using std::string;

//Base class for input formats
//Only support file input
class InputFormat
{
public:
  string &input;
  InputFormat(string &split) : input(split) {}
  virtual ~InputFormat();
  virtual int formatting() = 0;
};

//Default input format
//(k_i,v_i) = (the offset of the first character in line i,the text(string) of line i)
class TextFormat : public InputFormat
{
public:
  virtual int formatting(list<int> &keys, list<string> &values);
};

//KeyValue input format
//the file must using separator to separate key and value in a line
template <class K, class V>
class KeyValueFormat : public InputFormat
{
public:
  const char SEPARATOR = '\t';
  virtual int formatting(list<K> &keys, list<V> &values);
};

virtual int TextFormat::formatting(list<int> &keys, list<string> &values)
{
  int temp_key = 0, str_len = 0;
  int input_size = input.size();
  string temp_str;
  for (i = 0; i < input_size; i++)
  {
    if (input[i] == '\n')
    {
      temp_str = input.substr(temp_key, str_len);
      keys.push_back(temp_key);
      values.push_back(temp_str);
      str_len = 0;
      temp_key = i + 1;
      continue;
    }
    str_len++;
  }
}

template <class K, class V>
virtual  int KeyValueFormat::formatting(list<K> &keys, list<V> &values)
{

}

} // namespace mapreduce
#endif //MAPREDUCE_INPUTFORMAT