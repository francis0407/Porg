
// HashAlgorithm.h
#ifndef MAPREDUCE_HASHALGORITHM
#define MAPREDUCE_HASHALGORITHM
#include "common.h"
namespace mapreduce
{

using std::string;
using std::stringstream;

//Abstract class for Hash
//change keyvalue to int
template <class K>
class HashAlgorithm
{
  public:
    int size;
    HashAlgorithm(int s) : size(s) {}
    virtual int hash(K key) = 0;
};

template <class K>
class SDBMHash : public HashAlgorithm<K>
{
  public:
    SDBMHash(int s) : HashAlgorithm<K>(s) {}
    int hash(K input);
};

template <class K>
class BKDRHash : public HashAlgorithm<K>
{
  public:
    BKDRHash(int s) : HashAlgorithm<K>(s) {}
    int hash(K input);
};

template <class K>
class APHash : public HashAlgorithm<K>
{
  public:
    APHash(int s) : HashAlgorithm<K>(s) {}
    int hash(K input);
};
template <class K>
int to_string(char *&str, K input);

template <>
int to_string<int>(char *&str, int input);

template <>
int to_string<float>(char *&str, float input);

template <>
int to_string<string>(char *&str, string input);
} // namespace mapreduce
#endif //MAPREDUCE_HASHALGORITHM
