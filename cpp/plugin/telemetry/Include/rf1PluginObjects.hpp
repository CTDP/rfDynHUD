#ifndef _RF1_PLUGINOBJECT
#define _RF1_PLUGINOBJECT

#include <windows.h>


// forward referencing stuff
class PluginObjectInfo;


//##########################################################################
//# typedefs for dll functions - easier to use a typedef than to type
//# out the crazy syntax for declaring and casting function pointers
//##########################################################################
typedef const char* ( __cdecl *GETPLUGINNAME )();
typedef unsigned ( __cdecl *GETPLUGINVERSION )();
typedef unsigned ( __cdecl *GETPLUGINOBJECTCOUNT )();
typedef PluginObjectInfo* ( __cdecl *GETPLUGINOBJECTINFO ) ( const unsigned uIndex );
typedef PluginObjectInfo* ( __cdecl *GETPLUGINOBJECTINFO ) ( const unsigned uIndex );
typedef float ( __cdecl *GETPLUGINSYSTEMVERSION ) ();


//plugin object types
enum PluginObjectType
{
    PO_VIDEO_EXPORT = 0x00000001,
    PO_RFMODIFIER   = 0x00000002,
    PO_HWPLUGIN     = 0x00000003,
    PO_GAMESTATS    = 0x00000004,
    PO_NCPLUGIN     = 0x00000005,
    PO_MOTION       = 0x00000006,
    PO_IRCPLUGIN    = 0x00000007,
    PO_IVIBE        = 0x00000008,
    PO_INTERNALS    = 0x00000009,
};


//##########################################################################
//#  Plugin Object Property
//#   - can be used to expose pluginobject settings to rFactor.  
//#     In practice this feature has gone almost entirely unused
//##########################################################################
enum POPType
{
    POPTYPE_INT,
    POPTYPE_FLOAT,
    POPTYPE_STRING,
};

static char POPTypeNames[3][64] = 
{
    "POPTYPE_INT",
    "POPTYPE_FLOAT",
    "POPTYPE_STRING",
};

const unsigned POP_MAXNAME = 32;
const unsigned POP_MAXDESC = 256;

struct PluginObjectProperty
{
    union
    {
        int   iValue;
        float fValue;
        char* szValue;
    };
    
    POPType uPropertyType;
    char szName[POP_MAXNAME];
    char szDesc[POP_MAXDESC];
};

//##########################################################################
//#  PluginObject 
//#    - interface used by plugin classes.
//##########################################################################
class PluginObject
{
  public:
    
    PluginObject() {}
    virtual ~PluginObject(){};
    
    virtual void Destroy() = 0;
    virtual class PluginObjectInfo* GetInfo() = 0;
    
    virtual unsigned GetPropertyCount() const = 0;
    virtual PluginObjectProperty* GetProperty( const unsigned uIndex ) = 0;
    virtual PluginObjectProperty* GetProperty( const char* szName ) = 0;
};

//##########################################################################
//#  PluginObjectInfo
//#    - interface used by plugin info classes.
//#      the purpose of the plugin info classes is to allow the game to get 
//#      information about and instantiate the plugin objects contained in 
//#      a dll without needing to know anything about the PO in advance
//##########################################################################
class PluginObjectInfo
{
  public:
    
    virtual ~PluginObjectInfo() {};
    
    virtual const char* GetName() const = 0;
    virtual const char* GetFullName() const = 0;
    virtual const char* GetDesc() const = 0;
    virtual const unsigned GetType() const = 0;
    virtual const char* GetSubType() const = 0;
    virtual const unsigned GetVersion() const = 0;
    virtual void* Create() const = 0;
};

#endif _RF1_PLUGINOBJECT
