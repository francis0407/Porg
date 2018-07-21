
// InputFormat.h

#ifndef MAPREDUCE_INPUTFORMAT
#define MAPREDUCE_INPUTFORMAT

#include "common.h"

namespace mapreduce
{

using std::list;
using std::string;

/* Base class for input formats
 * Only support file input
 * An abstract class
 */
class InputFormat
{
public:
  string &input;
  InputFormat(string &split) : input(split) {} //constructor
  virtual ~InputFormat(){};
};

/* inherited from ``InputFormat``
 * Default input format
 * (k_i,v_i) = (the offset of the first character in line i,the text(string) of line i)
 */
class TextFormat : public InputFormat
{
public:
  TextFormat(string &input) : InputFormat(input) {} //constructor
  virtual int formatting(list<int> &keys, list<string> &values);
};

/* inherited from ``InputFormat``
 * KeyValue input format
 * the file must using separator to separate key and value in a line
 */
template <class K, class V>
class KeyValueFormat : public InputFormat
{
public:
  const static char SEPARATOR = '\t';
  KeyValueFormat(string &input) : InputFormat(input) {} //constructor
  virtual int formatting(list<K> &keys, list<V> &values);
};

} // namespace mapreduce
#endif //MAPREDUCE_INPUTFORMAT