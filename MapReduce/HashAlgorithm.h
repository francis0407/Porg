
// HashAlgorithm.h

#include "common.h"
namespace mapreduce{

//Abstract class for Hash
template<class K>
class HashAlgorithm{
 public:
    int size;
    HashAlgorithm(int s):size(s){}

    virtual int hash(K key) = 0;
};

}