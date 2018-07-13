#include "Mapper.h"
#include "InputFormat.h"
#include <string>
namespace mapreduce{
using std::string;
class TestMapper : public Mapper<int,string,int,string,TextFormat>{
 public:
  virtual Map(int key,string value){
      //...
      context.write(key,value);
  }

};
}