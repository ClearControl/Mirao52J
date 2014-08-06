
typedef void *PVOID;

typedef void *LPVOID;

typedef PVOID HANDLE;

typedef unsigned char UCHAR;

typedef UCHAR *PUCHAR;

typedef char CHAR;

typedef CHAR *PCHAR;

typedef unsigned long ULONG;

typedef ULONG *PULONG;

typedef const wchar_t* LPCWSTR;

typedef LPCWSTR LPCTSTR; 

typedef unsigned long DWORD;

typedef DWORD *LPDWORD;

typedef unsigned short WORD;

typedef WORD *LPWORD;

typedef struct _OVERLAPPED {
  ULONG_PTR Internal;
  ULONG_PTR InternalHigh;
  union {
    struct {
      DWORD Offset;
      DWORD OffsetHigh;
    };
    PVOID  Pointer;
  };
  HANDLE    hEvent;
} OVERLAPPED, *LPOVERLAPPED;

typedef struct _SECURITY_ATTRIBUTES {
  DWORD  nLength;
  LPVOID lpSecurityDescriptor;
  BOOL   bInheritHandle;
} SECURITY_ATTRIBUTES, *PSECURITY_ATTRIBUTES, *LPSECURITY_ATTRIBUTES;







