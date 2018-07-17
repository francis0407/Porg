
// HashAlgorithm.h
#ifndef MAPREDUCE_HASHALGORITHM
#define MAPREDUCE_HASHALGORITHM
#include "common.h"
namespace mapreduce{

//Abstract class for Hash
//change keyvalue to int
template<class K>
int to_string(char *&str, K input)
{
    return 0;
}

template<>
int to_string<int>(char *&str, int input){
    stringstream ss;
    string a;
    ss << input;
    ss >> a;
    str = &a[0];
    return 1;
}

template<>
int to_string<float>(char *&str, float input){
    stringstream ss;
    string a;
    ss << input;
    ss >> a;
    str = &a[0];
    return 1;
}

template<>
int to_string<string>(char *&str, string input){
    str = &input[0];
    return 1;
}


template<class K>
class HashAlgorithm{
public:
    int size;
    HashAlgorithm(int s):size(s){

    }
    virtual int hash(K key) = 0;
};

template<class K>
class SDBMHash : public HashAlgorithm<K>{
public:
    SDBMHash(int s):HashAlgorithm<K>(s){
    }
    int hash(K input)
    {
        char *str;
        to_string(str, input);
        int hash = 0;
        while (int ch = (int)*str++)
        {
            hash = (int)ch + (hash << 6) + (hash << 16) - hash;
        }
        return hash % this->size;
    }
};

template<class K>
class BKDRHash : public HashAlgorithm<K>{
public:
    BKDRHash(int s):HashAlgorithm<K>(s){
    }
    int hash(K input)
    {
        char *str;
        to_string(str, input);
        int hash = 0;
        while (int ch = (int)*str++)
        {
            hash = hash * 131 + ch;
        }
        return hash % this->size;
    }
};

template<class K>
class APHash : public HashAlgorithm<K>{
public:
    APHash(int s):HashAlgorithm<K>(s){
    }
    int hash(K input)
    {
        char *str;
        to_string(str, input);
        int hash = 0, ch;
        for (long i = 0; ch = (int)*str++; i++)
        {
            if ((i & 1) == 0)
            {
                hash ^= ((hash << 7) ^ ch ^ (hash >> 3));
            }
            else
            {
                hash ^= (~((hash << 11) ^ ch ^ (hash >> 5)));
            }
        }
        return hash % this->size;
    }
};
}
#endif //MAPREDUCE_HASHALGORITHM