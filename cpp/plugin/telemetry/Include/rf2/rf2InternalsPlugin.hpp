//‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹
//›                                                                         ﬁ
//› Module: Header file for internals plugin                                ﬁ
//›                                                                         ﬁ
//› Description: Interface declarations for internals plugin                ﬁ
//›                                                                         ﬁ
//› This source code module, and all information, data, and algorithms      ﬁ
//› associated with it, are part of isiMotor Technology (tm).               ﬁ
//›                 PROPRIETARY AND CONFIDENTIAL                            ﬁ
//› Copyright (c) 1996-2011 Image Space Incorporated.  All rights reserved. ﬁ
//›                                                                         ﬁ
//› Change history:                                                         ﬁ
//›   tag.2005.11.29: created                                               ﬁ
//›                                                                         ﬁ
//ﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂ

#ifndef _RF2_INTERNALS_PLUGIN_HPP_
#define _RF2_INTERNALS_PLUGIN_HPP_

#include "rf2PluginObjects.hpp"     // base class for plugin objects to derive from
#include <math.h>                // for sqrt()
#include <windows.h>             // for HWND


// IMPORTANT NOTE: Many of the interfaces here are not fully supported in certain versions of
// rFactor.  In particular, the rendering and physics (including tire and differential) functions
// can currently only be used in rF Pro.


// rF currently uses 4-byte packing ... whatever the current packing is will
// be restored at the end of this include with another #pragma.
#pragma pack( push, 4 )


//⁄ƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒø
//≥ Version01 Structures                                                   ≥
//¿ƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒŸ

struct TelemVect3
{
  double x, y, z;

  void Set( const double a, const double b, const double c )  { x = a; y = b; z = c; }

  // Allowed to reference as [0], [1], or [2], instead of .x, .y, or .z, respectively
        double &operator[]( long i )               { return( ( (double *) &x )[ i ] ); }
  const double &operator[]( long i ) const         { return( ( (double *) &x )[ i ] ); }
};


struct TelemQuat
{
  double w, x, y, z;

  // Convert this quaternion to a matrix
  void ConvertQuatToMat( TelemVect3 ori[3] ) const
  {
    const double x2 = x + x;
    const double xx = x * x2;
    const double y2 = y + y;
    const double yy = y * y2;
    const double z2 = z + z;
    const double zz = z * z2;
    const double xz = x * z2;
    const double xy = x * y2;
    const double wy = w * y2;
    const double wx = w * x2;
    const double wz = w * z2;
    const double yz = y * z2;
    ori[0][0] = (double) 1.0 - ( yy + zz );
    ori[0][1] = xy - wz;
    ori[0][2] = xz + wy;
    ori[1][0] = xy + wz;
    ori[1][1] = (double) 1.0 - ( xx + zz );
    ori[1][2] = yz - wx;
    ori[2][0] = xz - wy;
    ori[2][1] = yz + wx;
    ori[2][2] = (double) 1.0 - ( xx + yy );
  }

  // Convert a matrix to this quaternion
  void ConvertMatToQuat( const TelemVect3 ori[3] )
  {
    const double trace = ori[0][0] + ori[1][1] + ori[2][2] + (double) 1.0;
    if( trace > 0.0625f )
    {
      const double sqrtTrace = sqrt( trace );
      const double s = (double) 0.5 / sqrtTrace;
      w = (double) 0.5 * sqrtTrace;
      x = ( ori[2][1] - ori[1][2] ) * s;
      y = ( ori[0][2] - ori[2][0] ) * s;
      z = ( ori[1][0] - ori[0][1] ) * s;
    }
    else if( ( ori[0][0] > ori[1][1] ) && ( ori[0][0] > ori[2][2] ) )
    {
      const double sqrtTrace = sqrt( (double) 1.0 + ori[0][0] - ori[1][1] - ori[2][2] );
      const double s = (double) 0.5 / sqrtTrace;
      w = ( ori[2][1] - ori[1][2] ) * s;
      x = (double) 0.5 * sqrtTrace;
      y = ( ori[0][1] + ori[1][0] ) * s;
      z = ( ori[0][2] + ori[2][0] ) * s;
    }
    else if( ori[1][1] > ori[2][2] )
    {
      const double sqrtTrace = sqrt( (double) 1.0 + ori[1][1] - ori[0][0] - ori[2][2] );
      const double s = (double) 0.5 / sqrtTrace;
      w = ( ori[0][2] - ori[2][0] ) * s;
      x = ( ori[0][1] + ori[1][0] ) * s;
      y = (double) 0.5 * sqrtTrace;
      z = ( ori[1][2] + ori[2][1] ) * s;
    }
    else
    {
      const double sqrtTrace = sqrt( (double) 1.0 + ori[2][2] - ori[0][0] - ori[1][1] );
      const double s = (double) 0.5 / sqrtTrace;
      w = ( ori[1][0] - ori[0][1] ) * s;
      x = ( ori[0][2] + ori[2][0] ) * s;
      y = ( ori[1][2] + ori[2][1] ) * s;
      z = (double) 0.5 * sqrtTrace;
    }
  }
};


struct TelemWheelV01
{
  double mSuspensionDeflection;  // meters
  double mRideHeight;            // meters
  double mSuspForce;             // pushrod load in Newtons
  double mBrakeTemp;             // Celsius
  double mBrakePressure;         // currently 0.0-1.0, depending on driver input and brake balance; will convert to true brake pressure (kPa) in future

  double mRotation;              // radians/sec
  double mLateralPatchVel;       // lateral velocity at contact patch
  double mLongitudinalPatchVel;  // longitudinal velocity at contact patch
  double mLateralGroundVel;      // lateral velocity at contact patch
  double mLongitudinalGroundVel; // longitudinal velocity at contact patch
  double mCamber;                // radians (positive is left for left-side wheels, right for right-side wheels)
  double mLateralForce;          // Newtons
  double mLongitudinalForce;     // Newtons
  double mTireLoad;              // Newtons

  double mGripFract;             // an approximation of what fraction of the contact patch is sliding
  double mPressure;              // kPa (tire pressure)
  double mTemperature[3];        // Kelvin (subtract 273.16 to get Celsius), left/center/right (not to be confused with inside/center/outside!)
  double mWear;                  // wear (0.0-1.0, fraction of maximum) ... this is not necessarily proportional with grip loss
  char mTerrainName[16];         // the material prefixes from the TDF file
  unsigned char mSurfaceType;    // 0=dry, 1=wet, 2=grass, 3=dirt, 4=gravel, 5=rumblestrip
  bool mFlat;                    // whether tire is flat
  bool mDetached;                // whether wheel is detached

  double mVerticalTireDeflection;// how much is tire deflected from its (speed-sensitive) radius
  double mWheelYLocation;        // wheel's y location relative to vehicle y location

  // 2009.02.03 - ( (double *) mExpansion )[ 0 ] is now the current toe angle w.r.t. the vehicle
  unsigned char mExpansion[64];  // for future use
};


// Our world coordinate system is left-handed, with +y pointing up.
// The local vehicle coordinate system is as follows:
//   +x points out the left side of the car (from the driver's perspective)
//   +y points out the roof
//   +z points out the back of the car
// Rotations are as follows:
//   +x pitches up
//   +y yaws to the right
//   +z rolls to the right
// Note that ISO vehicle coordinates (+x forward, +y right, +z upward) are
// right-handed.  If you are using that system, be sure to negate any rotation
// or torque data because things rotate in the opposite direction.  In other
// words, a -z velocity in rFactor is a +x velocity in ISO, but a -z rotation
// in rFactor is a -x rotation in ISO!!!

struct TelemInfoV01
{
  // Time
  long mID;                      // slot ID (note that it can be re-used in multiplayer after someone leaves)
  double mDeltaTime;             // time since last update (seconds)
  double mElapsedTime;           // game session time
  long mLapNumber;               // current lap number
  double mLapStartET;            // time this lap was started
  char mVehicleName[64];         // current vehicle name
  char mTrackName[64];           // current track name

  // Position and derivatives
  TelemVect3 mPos;               // world position in meters
  TelemVect3 mLocalVel;          // velocity (meters/sec) in local vehicle coordinates
  TelemVect3 mLocalAccel;        // acceleration (meters/sec^2) in local vehicle coordinates

  // Orientation and derivatives
  TelemVect3 mOri[3];            // rows of orientation matrix (use TelemQuat conversions if desired), also converts local
                                 // vehicle vectors into world X, Y, or Z using dot product of rows 0, 1, or 2 respectively
  TelemVect3 mLocalRot;          // rotation (radians/sec) in local vehicle coordinates
  TelemVect3 mLocalRotAccel;     // rotational acceleration (radians/sec^2) in local vehicle coordinates

  // Vehicle status
  long mGear;                    // -1=reverse, 0=neutral, 1+=forward gears
  double mEngineRPM;             // engine RPM
  double mEngineWaterTemp;       // Celsius
  double mEngineOilTemp;         // Celsius
  double mClutchRPM;             // clutch RPM

  // Driver input
  double mUnfilteredThrottle;    // ranges  0.0-1.0
  double mUnfilteredBrake;       // ranges  0.0-1.0
  double mUnfilteredSteering;    // ranges -1.0-1.0 (left to right)
  double mUnfilteredClutch;      // ranges  0.0-1.0

  // Filtered input (various adjustments for rev or speed limiting, TC, ABS?, speed sensitive steering, clutch work for semi-automatic shifting, etc.)
  double mFilteredThrottle;      // ranges  0.0-1.0
  double mFilteredBrake;         // ranges  0.0-1.0
  double mFilteredSteering;      // ranges -1.0-1.0 (left to right)
  double mFilteredClutch;        // ranges  0.0-1.0

  // Misc
  double mSteeringArmForce;      // force on steering arms
  double mFront3rdDeflection;    // deflection at front 3rd spring
  double mRear3rdDeflection;     // deflection at rear 3rd spring

  // Aerodynamics
  double mFrontWingHeight;       // front wing height
  double mFrontRideHeight;       // front ride height
  double mRearRideHeight;        // rear ride height
  double mDrag;                  // drag
  double mFrontDownforce;        // front downforce
  double mRearDownforce;         // rear downforce

  // State/damage info
  double mFuel;                  // amount of fuel (liters)
  double mEngineMaxRPM;          // rev limit
  unsigned char mScheduledStops; // number of scheduled pitstops
  bool  mOverheating;            // whether overheating icon is shown
  bool  mDetached;               // whether any parts (besides wheels) have been detached
  unsigned char mDentSeverity[8];// dent severity at 8 locations around the car (0=none, 1=some, 2=more)
  double mLastImpactET;          // time of last impact
  double mLastImpactMagnitude;   // magnitude of last impact
  TelemVect3 mLastImpactPos;     // location of last impact

  // Future use
  // 2008.09.08 - ( (double *) mExpansion )[ 0 ] is now the current engine torque (including additive torque)
  // 2010.01.07 - ( (long *) mExpansion )[ 2 ] is now the current sector (zero-based) with the pitlane stored in the sign bit (example: entering pits from third sector gives 0x80000002)
  unsigned char mExpansion[256]; // for future use (note that the slot ID has been moved to mID above)

  // keeping this at the end of the structure to make it easier to replace in future versions
  TelemWheelV01 mWheel[4];       // wheel info (front left, front right, rear left, rear right)
};


struct GraphicsInfoV01
{
  TelemVect3 mCamPos;            // camera position
  TelemVect3 mCamOri[3];         // rows of orientation matrix (use TelemQuat conversions if desired), also converts local
  HWND mHWND;                    // app handle

  double mAmbientRed;
  double mAmbientGreen;
  double mAmbientBlue;
};


struct GraphicsInfoV02 : public GraphicsInfoV01
{
  long mID;                      // slot ID being viewed (-1 if invalid)

  // Camera types (some of these may only be used for *setting* the camera type in WantsToViewVehicle())
  //    0  = TV cockpit
  //    1  = cockpit
  //    2  = nosecam
  //    3  = swingman
  //    4  = trackside (nearest)
  //    5  = onboard000
  //       :
  //       :
  // 1004  = onboard999
  // 1005+ = (currently unsupported, in the future may be able to set/get specific trackside camera)
  long mCameraType;              // see above comments for possible values

  unsigned char mExpansion[128]; // for future use (possibly camera name)
};


struct CameraControlInfoV01
{
  long mID;                      // slot ID to view
  long mCameraType;              // see GraphicsInfoV02 comments for values

  unsigned char mExpansion[128]; // for future use (possibly camera name & positions/orientations)
};


struct MessageInfoV01
{
  char mText[128];               // message to display

  unsigned char mExpansion[128]; // for future use (possibly what color, what font, whether to attempt translation, and seconds to display)
};


struct VehicleScoringInfoV01
{
  long mID;                      // slot ID (note that it can be re-used in multiplayer after someone leaves)
  char mDriverName[32];          // driver name
  char mVehicleName[64];         // vehicle name
  short mTotalLaps;              // laps completed
  signed char mSector;           // 0=sector3, 1=sector1, 2=sector2 (don't ask why)
  signed char mFinishStatus;     // 0=none, 1=finished, 2=dnf, 3=dq
  double mLapDist;               // current distance around track
  double mPathLateral;           // lateral position with respect to *very approximate* "center" path
  double mTrackEdge;             // track edge (w.r.t. "center" path) on same side of track as vehicle

  double mBestSector1;           // best sector 1
  double mBestSector2;           // best sector 2 (plus sector 1)
  double mBestLapTime;           // best lap time
  double mLastSector1;           // last sector 1
  double mLastSector2;           // last sector 2 (plus sector 1)
  double mLastLapTime;           // last lap time
  double mCurSector1;            // current sector 1 if valid
  double mCurSector2;            // current sector 2 (plus sector 1) if valid
  // no current laptime because it instantly becomes "last"

  short mNumPitstops;            // number of pitstops made
  short mNumPenalties;           // number of outstanding penalties
  bool mIsPlayer;                // is this the player's vehicle

  signed char mControl;          // who's in control: -1=nobody (shouldn't get this), 0=local player, 1=local AI, 2=remote, 3=replay (shouldn't get this)
  bool mInPits;                  // between pit entrance and pit exit (not always accurate for remote vehicles)
  unsigned char mPlace;          // 1-based position
  char mVehicleClass[32];        // vehicle class

  // Dash Indicators
  double mTimeBehindNext;        // time behind vehicle in next higher place
  long mLapsBehindNext;          // laps behind vehicle in next higher place
  double mTimeBehindLeader;      // time behind leader
  long mLapsBehindLeader;        // laps behind leader
  double mLapStartET;            // time this lap was started

  // Position and derivatives
  TelemVect3 mPos;               // world position in meters
  TelemVect3 mLocalVel;          // velocity (meters/sec) in local vehicle coordinates
  TelemVect3 mLocalAccel;        // acceleration (meters/sec^2) in local vehicle coordinates

  // Orientation and derivatives
  TelemVect3 mOri[3];            // rows of orientation matrix (use TelemQuat conversions if desired), also converts local
                                 // vehicle vectors into world X, Y, or Z using dot product of rows 0, 1, or 2 respectively
  TelemVect3 mLocalRot;          // rotation (radians/sec) in local vehicle coordinates
  TelemVect3 mLocalRotAccel;     // rotational acceleration (radians/sec^2) in local vehicle coordinates

  // Future use
  unsigned char mExpansion[128]; // for future use
};


struct ScoringInfoV01
{
  char mTrackName[64];           // current track name
  long mSession;                 // current session (0=testday 1-4=practice 5-8=qual 9=warmup 10-13=race)
  double mCurrentET;             // current time
  double mEndET;                 // ending time
  long  mMaxLaps;                // maximum laps
  double mLapDist;               // distance around track
  char *mResultsStream;          // results stream additions since last update (newline-delimited and NULL-terminated)

  long mNumVehicles;             // current number of vehicles

  // Game phases:
  // 0 Before session has begun
  // 1 Reconnaissance laps (race only)
  // 2 Grid walk-through (race only)
  // 3 Formation lap (race only)
  // 4 Starting-light countdown has begun (race only)
  // 5 Green flag
  // 6 Full course yellow / safety car
  // 7 Session stopped
  // 8 Session over
  unsigned char mGamePhase;   

  // Yellow flag states (applies to full-course only)
  // -1 Invalid
  //  0 None
  //  1 Pending
  //  2 Pits closed
  //  3 Pit lead lap
  //  4 Pits open
  //  5 Last lap
  //  6 Resume
  //  7 Race halt (not currently used)
  signed char mYellowFlagState;

  signed char mSectorFlag[3];      // whether there are any local yellows at the moment in each sector (not sure if sector 0 is first or last, so test)
  unsigned char mStartLight;       // start light frame (number depends on track)
  unsigned char mNumRedLights;     // number of red lights in start sequence
  bool mInRealtime;                // in realtime as opposed to at the monitor
  char mPlayerName[32];            // player name (including possible multiplayer override)
  char mPlrFileName[64];           // may be encoded to be a legal filename

  // weather
  double mDarkCloud;               // cloud darkness? 0.0-1.0
  double mRaining;                 // raining severity 0.0-1.0
  double mAmbientTemp;             // temperature (Celsius)
  double mTrackTemp;               // temperature (Celsius)
  TelemVect3 mWind;                // wind speed
  double mOnPathWetness;           // on main path 0.0-1.0
  double mOffPathWetness;          // on main path 0.0-1.0

  // Future use
  unsigned char mExpansion[256];

  // keeping this at the end of the structure to make it easier to replace in future versions
  VehicleScoringInfoV01 *mVehicle; // array of vehicle scoring info's
};


struct CommentaryRequestInfoV01
{
  char mName[32];                  // one of the event names in the commentary INI file
  double mInput1;                  // first value to pass in (if any)
  double mInput2;                  // first value to pass in (if any)
  double mInput3;                  // first value to pass in (if any)
  bool mSkipChecks;                // ignores commentary detail and random probability of event

  // constructor (for noobs, this just helps make sure everything is initialized to something reasonable)
  CommentaryRequestInfoV01()       { mName[0] = 0; mInput1 = 0.0; mInput2 = 0.0; mInput3 = 0.0; mSkipChecks = false; }
};


//⁄ƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒø
//≥ Version02 Structures                                                   ≥
//¿ƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒŸ

struct NewVehicleDataV01
{
  char mVehFile[32];               // Name of the VEH file (no path, with or without extension .VEH)
  char mDriverName[32];            // If empty, will use default from VEH file
  unsigned char mUpgradePack[8];   // Coded upgrades (recommend to set to all zeroes if unknown)
  char mSkin[32];                  // Skin name (set empty for default)
  char mHelmet[32];                // Helmet name (set empty for default)
};


struct VehicleAndPhysicsV01 : public NewVehicleDataV01
{
  long mID;                        // slot ID (note that it can be re-used in multiplayer after someone leaves)
  bool mIsPlayer;                  // is this the player's vehicle
  signed char mControl;            // who's in control: -1=nobody (shouldn't get this), 0=local player, 1=local AI, 2=remote, 3=replay (shouldn't get this)
  char mPhysicsFile[256];          // relative path to physics file
  char mSetupFile[256];            // relative path to vehicle setup file
};

struct StartingVehicleLocationV01
{
  long mID;                        // slot ID (note that it can be re-used in multiplayer after someone leaves)
  TelemVect3 mPos;                 // Position (specifies where the *front* of the vehicle should be at ground level)
  TelemVect3 mOri[3];              // Orientation matrix (use conversion from TelemQuat if desired)
};

enum PluginControlRequest
{
  PCR_NONE = 0,                    // No control desired over given vehicle
  PCR_STATE,                       // State control (substitution in AI physics of x&z location and optionally much more)
  PCR_PHYSICS,                     // Is capable of physics control
  //-------------
  PCR_MAXIMUM
};


struct WheelStateV01
{
  double mRotation;                // radians/sec
  double mBrakeTemp;               // Celsius
  double mYLocation;               // wheel's y location relative to vehicle y location
};

struct VehicleStateV01
{
  long mID;                        // slot ID (note that it can be re-used in multiplayer after someone leaves)

  // this is the time at which this vehicle state is valid
  double mET;                      // elapsed time

  // special
  bool mPurgeFlag;                 // flag to remove any previously stored states (could be used to start a new lap, for example)
  bool mCollidable;                // can this vehicle be collided with?
  double mTransparentProximity;    // for automatic transparency: 0.0 = off, 2.0-62.0 = fully visible at this distance from the vehicle that camera is focusing on, and totally invisible at 1/4th of that distance
  double mManualTransparency;      // for manual transparency: 0.0 = visible, 1.0 = invisible, can be used to indicate confidence in data

  // location
  // Please see note above struct TelemInfoV01 for rFactor coordinate system and conversion info!
  bool mPosYValid;                 // whether the y (height) coordinate is valid (if invalid, will use HAT)
  bool mOriValid;                  // whether the orientation matrix is valid (if invalid, will use AI simulation)
  bool mHeadingValid;              // only if full orientation is invalid, we'll try to use simple heading instead
  TelemVect3 mPos;                 // Position (x and z values must *always* be valid)
  TelemVect3 mOri[3];              // Orientation matrix (use conversion from TelemQuat if desired)
  double mHeading;                 // Heading angle, in radians

  // motion
  bool mVelValid;                  // whether velocity is valid (if invalid, will use AI simulation)
  bool mRotValid;                  // whether rotation is valid (if invalid, will use AI simulation)
  TelemVect3 mVel;                 // World velocity (sorry, telemetry reports local velocity but we can't convert if optional orientation is invalid!)
  TelemVect3 mRot;                 // World rotation (sorry, telemetry reports local rotation but we can't convert if optional orientation is invalid!)

  // engine, input, gear
  bool mGearValid;                 // whether gear is valid
  bool mRPMValid;                  // whether engine RPM is valid
  bool mInputsValid;               // whether throttle, brake, and steering are valid
  long mGear;                      // -1=reverse, 0=neutral, 1+=forward gears
  double mEngineRPM;               // engine RPM (rotations per minute)
  double mThrottle;                // ranges  0.0-1.0
  double mBrake;                   // ranges  0.0-1.0
  double mSteering;                // ranges -1.0-1.0 (left to right)

  // wheel info
  bool mWheelRotationsValid;       // whether wheel rotations are valid
  bool mWheelBrakeTempsValid;      // whether wheel brake temps are valid
  bool mWheelYLocationsValid;      // whether wheel y locations are valid
  WheelStateV01 mWheel[4];         // wheel information

  // tag.2009.01.21 - added this expansion area ... currently rFactor is clearing this memory
  // due to backwards-compatibility concerns, but we will eventually stop doing that when all
  // current plugins are re-compiled.
  // 2009.01.21 - mExpansion[ 0 ] is now used for brakelight override: 0=invalid (rF control), 1=force off, 2=force on
  unsigned char mExpansion[256];

  // This structure is *not* initialized upon the call to GetVehicleState(), so if you plan to
  // modify the vehicle state and return true, we *highly* recommend that you first call this
  // Clear() function before modifying the vehicle state; it will initialize all "valid flags"
  // to false and set everything else to zero (in a hacky but efficient manner).  If you do not
  // call it, make sure all "valid flags" are set properly!
  void Clear()                     { for( long i = 0; i < sizeof( VehicleStateV01 ); ++i ) ( (char *) this )[ i ] = 0; }
};


struct PhysicsAdditiveV01
{
  TelemVect3 mLocalForce;          // will be added to the standardized CG (vehicle minus fluids, wheels in default locations)
  TelemVect3 mLocalTorque;         // will be added to body's other torques
  double mEngineTorque;            // positive for power, negative for coast (engine braking)
  double mRearBrakeBias;           // rear brake bias fraction (0.0-1.0); please set to -1.0 if invalid
};


struct ISOTyreInitV01
{
  long mTyreID;                    // 0=FrontLeft, 1=FrontRight, 2=RearLeft, 3=RearRight
  bool mThermalMode;               // false = NonThermal, true = Thermal (from TBC file)
  double mPressurePSI;             // Tyre pressure
  union
  {
    double mCarcassTemp;           // OBSOLETE: will be removed at future date when existing plugins are updated
    double mSurfaceTemp;           // initial surface temp in Celsius, used for Thermal mode only
  };
  double mBulkTemp;                // mean bulk tread in Celsius, used for Thermal mode
};


struct ISOTyreInputV01
{
  long mTyreID;                    // 0=FrontLeft, 1=FrontRight, 2=RearLeft, 3=RearRight
  double mDT;                      // delta time to integrate through
  double mET;                      // elapsed time (at *end* of physics frame, not beginning)

  // Note: ISO definitions used here
  double mTyreLoad;                // Fz, or tyre normal load in Newtons (positive for upward force on tyre)
  double mSlipAngle;               // In radians, positive ISO lateral velocity (left)
  double mSlipRatio;               // Unitless, positive for traction
  double mPressurePSI;             // Tyre pressure
  double mForwardVelocity;         // VxWc_t, wheel center forward velocity in stationary tyre axes (different to car Vx for front steered wheel)
  double mTrackTemp;               // Track surface temperature in Celsius
  double mAmbientTemp;             // Ambient temperature in Celsius
  double mRollAngle;               // Positive about ISO x axis (neg camber on left wheel)

  double mSurfaceTemp;             // tread surface temperature in Celsius (input for Thermal mode only)
  double mBulkTemp;                // mean bulk tread in Celsius (input for Thermal mode only)

  // surface properties (can use either look-up or simple gain)
  char mTerrainName[16];           // the material prefixes from the TDF file
  unsigned char mSurfaceType;      // 0=dry, 1=wet, 2=grass, 3=dirt, 4=gravel, 5=rumblestrip
  double mSurfaceGain;             // friction multiplier should be exactly 1.0 for 'average' asphalt
  double mSurfaceProperty[3];      // not currently defined
};


struct ISOTyreOutputV01
{
  // Note: ISO definitions used here
  // The forces applied at the intersection of three planes: the ground plane, the wheel plane, and
  // a third plane perpendicular to the first two and containing the wheel center.
  double mLongitudinalForce;       // FxTy_t
  double mLateralForce;            // FyTy_t
  double mAligningMoment;          // MzTy_t
  double mPressurePSI;             // Tyre pressure
  double mSurfaceTemp;             // tread surface temperature in Celsius, used for Thermal mode only
  double mBulkTemp;                // mean bulk tread in Celsius, used for Thermal mode only
  double mCarcassTemp;             // mean tread/carcass in Celsius (used for Thermal mode only?)
  double mRollingResistance;       // FxRolRes
};


struct DifferentialInitV01
{
  double mRollingRadius;           // Average of driven wheels
  double mInitialTorque;           // Initial torque
};


struct DifferentialInputV01
{
  double mDT;                      // delta time to integrate through
  double mET;                      // elapsed time (at *end* of physics frame, not beginning)

  double mEngineRPM;               // engine speed
  double mThrottle;                // throttle position 0.0 - 1.0
  double mGearRatio;               // current gear ratio, estimated by ( engine RPM / diff input shaft RPM )

  double mFinalDriveInputRPM;      // rotational speed of input shaft
  TelemVect3 mLocalAccel;          // acceleration (meters/sec^2) in local vehicle coordinates (x is left, z is rearward)
  double mFinalDriveInputTorque;   // approximate input torque
  double mDifferentialWheelRPM;    // positive means right wheel is spinning faster (in forward direction)

  double mLapDistance;             // distance around track in meters
  double mSteering;                // steering wheel (-1.0 = left, 1.0 = right)
  TelemVect3 mLocalRot;            // rotation (radians/sec) in local vehicle coordinates (y is yaw rate, positive to right, I think)
};


struct DifferentialOutputV01
{
  double mTorque;                  // amount of torque transferred (should be positive unless differential is magical)
};


struct PhysicsTriangleV01
{
  TelemVect3 mVertex[3];           // three verts in clockwise
  char mTerrainName[16];           // the material prefixes from the TDF file
  unsigned char mSurfaceType;      // 0=dry, 1=wet, 2=grass, 3=dirt, 4=gravel, 5=rumblestrip
  double mSurfaceGain;             // friction multiplier should be exactly 1.0 for 'average' asphalt
  double mSurfaceProperty[3];      // not currently defined
};


struct TrackGeometryV01
{
  long mNumTriangles;              // number of relevant triangles
  PhysicsTriangleV01 *mTriangle;   // array of triangles
};


struct DriverInputV01
{
  // 'standard' inputs
  double mSteering;
  double mThrottle;
  double mHandbrake;
  double mBrakes;
  double mClutch;
  double mPowerDemand;             // KERS or other

  long mDirectManualShift;         // -2="no selection"/invalid, -1=reverse, 0=neutral, 1+=forward gear
  bool mShiftUp;
  bool mShiftDown;
  bool mShiftToNeutral;
  bool mTCOverride;
  bool mLaunchControl;

  // UNDONE - engine maps, diff maps, KERS maps here?

  // expansion
  double mUnusedFloat[8];
  long mUnusedInt[16];
  bool mUnusedBool[16];
};


struct SlotStateV01                // location and motion of a slot
{
  // Please see note above struct TelemInfoV01 for rFactor coordinate system and conversion info!
  long mID;                        // slot ID (note that it can be re-used in multiplayer after someone leaves)
  TelemVect3 mPos;                 // position
  TelemVect3 mOri[3];              // orientation matrix (use conversion from TelemQuat if desired)
  TelemVect3 mVel;                 // velocity (in world coordinates, not local!)
  TelemVect3 mRot;                 // rotation (in world coordinates, not local!)
};


struct PhysicsInputV01 : DriverInputV01
{
  // timing info
  double mDT;                      // delta time to integrate through
  double mET;                      // elapsed time (at *end* of physics frame, not beginning)

  // weather
  double mAirDensity;              // current air density (which can be derived from: track altitude, ambient temp, humidity, and pressure)
  double mHumidity;                // humidity (0.0-1.0)
  double mPressure;                // air pressure (kPa)
  double mAmbientTemp;             // temperature (Celsius)
  double mTrackTemp;               // temperature (Celsius)
  TelemVect3 mWind;                // wind speed

  // NOTE: if using InternalsPluginV04 or above, track geometry can be optionally provided at load time or in realtime (see interface).
  // track geometry (for whole vehicle in order to calculate aerodynamics and bottoming-out)
  TrackGeometryV01 mTrackGeometryForVehicle;
  // track geometry (for each wheel - smaller sets than for vehicle in order to calculate contact more quickly)
  TrackGeometryV01 mTrackGeometryForWheel[4];

  // miscellaneous
  long mPrivate[8];                // for private use
  bool mStartSim;                  // 1 for start (running?), 0 to stop?
  bool mBeacon[6];                 // 3 sectors, 3 spare
  bool mPitting;                   // in pitlane
  bool mCollision;                 // collision detected by internal rFactor physics (data below)

  TelemVect3 mCollisionExitVector; // this is an optional position change to be applied in order to exit the collision
  TelemVect3 mCollisionForce;      // collision force to be applied to main vehicle body
  TelemVect3 mCollisionTorque;     // collision torque to be applied to main vehicle body

  // location and motion data
  SlotStateV01 mInternalState;     // location and motion of this vehicle according to internal rFactor physics

  long mNumOpponents;              // number of other vehicles
  SlotStateV01 *mOpponentState;    // location and motion states
};


struct WheelOutputV01
{
  TelemVect3 mPos;                 // center of wheel, in world coordinates
  TelemVect3 mOri[3];              // orientation matrix (use conversion from TelemQuat if desired)
  double mSlipAngle;               // radians
  double mSlipRatio;               // %
  double mTireLoad;                // load on tire in Newtons
  double mRotation;                // radians/sec
  double mBrakeTemp;               // Celsius
  double mTreadTemp[3];            // left/center/right
  double mAirTemp;                 // Celsius

  unsigned char mExpansion[64];    // future use
};


struct PhysicsOutputV01
{
  // timing and state info
  double mET;                      // elapsed time (at which this output state is valid ... rFactor may extrapolate briefly to synch with graphics)
  bool mSkipInternalPhysics;       // request internal physics to be skipped for performance reasons; note that most valid flags probably need to be true for this to work well
  bool mUseInternalPhysics;        // this indicates that plugin physics are temporarily invalid; rFactor should use its own internal calculations
  long mPrivate[8];                // for private use

  // location and motion are required
  // Please see note above struct TelemInfoV01 for rFactor coordinate system and conversion info!
  TelemVect3 mPos;                 // position
  TelemVect3 mOri[3];              // orientation matrix (use conversion from TelemQuat if desired)
  TelemVect3 mVel;                 // velocity (in world coordinates, not local!)
  TelemVect3 mRot;                 // rotation (in world coordinates, not local!)

  bool mAccelValid;                // whether acceleration is valid
  TelemVect3 mAccel;               // linear acceleration (in world coordinates)

  // wheel info
  bool mWheelPosValid;             // whether wheel positions are valid
  unsigned char mWheelOriValid;    // 0=invalid, 1=spindle axis only (stored in mOri[0]; rFactor will spin wheel), 2=full orientation (rFactor will *not* spin wheel other than for graphical extrapolation)
  bool mTireParamsValid;           // slip angle, slip ratio, and tire load
  bool mWheelRotationValid;        // whether rotations are valid
  bool mWheelBrakeTempValid;       // whether brake temps are valid
  bool mWheelTreadTempValid;       // whether tread temps are valid
  bool mWheelAirTempValid;         // whether air temps are valid
  WheelOutputV01 mWheel[4];        // wheel info

  // engine, input, gear
  bool mGearValid;                 // whether gear is valid
  bool mRPMValid;                  // whether engine RPM is valid
  bool mInputsValid;               // whether throttle, brake, and steering are valid
  long mGear;                      // -1=reverse, 0=neutral, 1+=forward gears
  double mEngineRPM;               // engine RPM (rotations per minute)
  double mThrottle;                // ranges  0.0-1.0
  double mBrake;                   // ranges  0.0-1.0
  double mSteering;                // ranges -1.0-1.0 (left to right)

  unsigned char mExpansion[256];   // future use

  // For efficiency's sake, this structure is *not* initialized upon the call to GetPhysicsState().
  // Please make sure to either call the Clear() function before modifying the structure, or fill
  // in all required data plus the "valid flags".
  void Clear()                     { for( long i = 0; i < sizeof( PhysicsOutputV01 ); ++i ) ( (char *) this )[ i ] = 0; }
};


struct PhysicsOptionsV01
{
  unsigned char mTractionControl;  // 0 (off) - 3 (high)
  unsigned char mAntiLockBrakes;   // 0 (off) - 2 (high)
  unsigned char mStabilityControl; // 0 (off) - 2 (high)
  unsigned char mAutoShift;        // 0 (off), 1 (upshifts), 2 (downshifts), 3 (all)
  unsigned char mAutoClutch;       // 0 (off), 1 (on)
  unsigned char mInvulnerable;     // 0 (off), 1 (on)
  unsigned char mOppositeLock;     // 0 (off), 1 (on)
  unsigned char mSteeringHelp;     // 0 (off) - 3 (high)
  unsigned char mBrakingHelp;      // 0 (off) - 2 (high)
  unsigned char mSpinRecovery;     // 0 (off), 1 (on)
  unsigned char mAutoPit;          // 0 (off), 1 (on)
  unsigned char mAutoLift;         // 0 (off), 1 (on)
  unsigned char mAutoBlip;         // 0 (off), 1 (on)

  unsigned char mFuelMult;         // fuel multiplier (0x-7x)
  unsigned char mTireMult;         // tire wear multiplier (0x-7x)
  unsigned char mMechFail;         // mechanical failure setting; 0 (off), 1 (normal), 2 (timescaled)
  unsigned char mAllowPitcrewPush; // 0 (off), 1 (on)
  unsigned char mRepeatShifts;     // accidental repeat shift prevention (0-5; see PLR file)
  unsigned char mHoldClutch;       // for auto-shifters at start of race: 0 (off), 1 (on)
  unsigned char mAutoReverse;      // 0 (off), 1 (on)
  unsigned char mAlternateNeutral; // Whether shifting up and down simultaneously equals neutral
  float mManualShiftOverrideTime;  // time before auto-shifting can resume after recent manual shift
  float mAutoShiftOverrideTime;    // time before manual shifting can resume after recent auto shift
  float mSpeedSensitiveSteering;   // 0.0 (off) - 1.0
  float mSteerRatioSpeed;          // speed (m/s) under which lock gets expanded to full
};


struct EnvironmentInfoV01
{
  // TEMPORARY buffers (you should copy them if needed for later use) containing various paths that may be needed.  Each of these
  // could be relative ("UserData\") or full ("C:\BlahBlah\rFactorProduct\UserData\").
  // mPath[ 0 ] points to the UserData directory.
  // mPath[ 1 ] points to the CustomPluginOptions.ini filename.
  // (in the future, we may add paths for the current garage setup, fully upgraded physics files, etc., any other requests?)
  char *mPath[ 16 ];
  unsigned char mExpansion[256];   // future use
};


//⁄ƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒø
//¿ƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒŸ

#ifndef _IMAGEWARP_PLUGIN_H
 #define MHWDEVICE void*
 #define MHWTEXTURE void*
#else
 #define MHWDEVICE LPDIRECT3DDEVICE9
 #define MHWTEXTURE LPDIRECT3DTEXTURE9
#endif

// define some operator overloads for enums
#define NOTOVERLOAD(type)        inline type operator ~  (type e1) { type e3 = (type)~(long)e1; return e3; }
#define OROVERLOAD(type)         inline type  operator |  (type e1,  type e2) { return ((type)((long)e1 | (long)e2)); }
#define ANDOVERLOAD(type)        inline type  operator &  (type e1,  type e2) { return ((type)((long)e1 & (long)e2)); }
#define OREQUALSOVERLOAD(type)   inline type &operator |= (type &e1, type e2) { e1 = (type)((long)e1 | (long)e2); return e1; }
#define ANDEQUALSOVERLOAD(type)  inline type &operator &= (type &e1, type e2) { e1 = (type)((long)e1 & (long)e2); return e1; }

typedef enum ImageWarpFlagsV01
{
  IWARPV01_DEFAULT            = 0x0000L,
  IWARPV01_USEALTRENDERTARGET = 0x0001L,  // if set, use offscreen rendertarget
  IWARPV01_SWAPRENDERTARGETS  = 0x0002L,  // if set, swap rendertarget before PostDraw (required for gMotors double buffered post-processing)
  IWARPV01_UPDATERENDERTARGET = 0x0004L,  // if set, set rendertarget to "back buffer" every frame (should do in case current RT is not "back buffer"
  IWARPV01_ENDSCENEBEFOREWARP = 0x0008L,  // if set, call EndScene before PostDraw

  IWARPV01_FORCEDWORD         = 0x7fffffffL,
};
// instantiate operator overloads for this enum
OROVERLOAD (ImageWarpFlagsV01); ANDOVERLOAD (ImageWarpFlagsV01); OREQUALSOVERLOAD (ImageWarpFlagsV01); ANDEQUALSOVERLOAD (ImageWarpFlagsV01);

// undef these operator overloads 
#undef NOTOVERLOAD
#undef OROVERLOAD
#undef ANDOVERLOAD
#undef OREQUALSOVERLOAD
#undef ANDEQUALSOVERLOAD

class ImageWarpInitDataV01
{
 public:

  char mServerIP[32];
  long mServerPort;
  char mChannelName[256];
  double mChannelOffset;
  long mTargetHeight;
  long mTargetWidth;

  MHWDEVICE mD3DDevice;
  MHWTEXTURE mRenderTarget;

  ImageWarpInitDataV01() { memset( this, 0, sizeof( ImageWarpInitDataV01 ) ); }
};

class ImageWarpDynDataV01
{
 public:

  ImageWarpFlagsV01 mFlags;

  bool  mAppUseViewParams; // if true, app will use the plugin view parameters
  bool  mAppUseViewMatrix; // if true, app will use the plugin view matrix
  bool  mAppViewMatrixPre; // if true, app will pre-multiply the plugin view matrix, else post-multiply

  bool  mAppUseProjParams; // if true, app will use the plugin proj parametes
  bool  mAppUseProjMatrix; // if true, app will use the plugin proj matrix

  bool  mAppUseWrldMatrix; // if true, app will use the plugin wrld matrix

  bool  mPlgUseViewMatrix; // if true, plugin will init with the app view matrix
  bool  mPlgUseProjMatrix; // if true, plugin will init with the app proj matrix
  bool  mPlgUseWrldMatrix; // if true, plugin will init with the app wrld matrix

  // view parameters
  double mYaw;
  double mPitch;
  double mRoll;

  double mLeftAngle;
  double mRightAngle;
  double mBottomAngle;
  double mTopAngle;

  // proj parameters
  double mLeftClip;
  double mRightClip;
  double mBottomClip;
  double mTopClip;
  double mNearClip;
  double mFarClip;

  // view / proj matrix
  float mViewMatrix[4][4];
  float mProjMatrix[4][4];

  union
  {
    float mWorldOffset[4][4];
    float mSpares[16];
  };

  MHWTEXTURE mRenderTarget;

  ImageWarpDynDataV01() { memset( this, 0, sizeof( ImageWarpDynDataV01 ) ); }
};

//⁄ƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒø
//≥ Plugin classes used to access internals                                ≥
//¿ƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒŸ

// Note: use class InternalsPluginV01 and have exported function GetPluginVersion() return 1, or
//       use class InternalsPluginV02 and have exported function GetPluginVersion() return 2, etc.
class InternalsPlugin : public PluginObject
{
 public:

  // General internals methods
  InternalsPlugin() {}
  virtual ~InternalsPlugin() {}

  // GAME FLOW NOTIFICATIONS
  virtual void Startup( long version ) {}                      // sim startup with version * 1000
  virtual void Shutdown() {}                                   // sim shutdown

  virtual void Load() {}                                       // scene/track load
  virtual void Unload() {}                                     // scene/track unload

  virtual void StartSession() {}                               // session started
  virtual void EndSession() {}                                 // session ended

  virtual void EnterRealtime() {}                              // entering realtime (where the vehicle can be driven)
  virtual void ExitRealtime() {}                               // exiting realtime

  // SCORING OUTPUT
  virtual bool WantsScoringUpdates() { return( false ); }      // whether we want scoring updates
  virtual void UpdateScoring( const ScoringInfoV01 &info ) {}  // update plugin with scoring info (approximately once per second)

  // GAME OUTPUT
  virtual long WantsTelemetryUpdates() { return( 0 ); }        // whether we want telemetry updates (0=no 1=player-only 2=all vehicles)
  virtual void UpdateTelemetry( const TelemInfoV01 &info ) {}  // update plugin with telemetry info

  virtual bool WantsGraphicsUpdates() { return( false ); }     // whether we want graphics updates
  virtual void UpdateGraphics( const GraphicsInfoV01 &info ) {}// update plugin with graphics info

  // COMMENTARY INPUT
  virtual bool RequestCommentary( CommentaryRequestInfoV01 &info ) { return( false ); } // to use our commentary event system, fill in data and return true

  // GAME INPUT
  virtual bool HasHardwareInputs() { return( false ); }        // whether plugin has hardware plugins
  virtual void UpdateHardware( const double fDT ) {}           // update the hardware with the time between frames
  virtual void EnableHardware() {}                             // message from game to enable hardware
  virtual void DisableHardware() {}                            // message from game to disable hardware

  // See if the plugin wants to take over a hardware control.  If the plugin takes over the
  // control, this method returns true and sets the value of the double pointed to by the
  // second arg.  Otherwise, it returns false and leaves the double unmodified.
  virtual bool CheckHWControl( const char * const controlName, double &fRetVal ) { return false; }

  virtual bool ForceFeedback( double &forceValue ) { return( false ); } // alternate force feedback computation - return true if editing the value

  // VIDEO EXPORT
  virtual bool WantsVideoOutput() { return( false ); }         // whether we want to export video
  virtual bool VideoOpen( const char * const szFilename, double fQuality, unsigned short usFPS, unsigned long fBPS,
                          unsigned short usWidth, unsigned short usHeight, char *cpCodec = NULL ) { return( false ); } // open video output file
  virtual void VideoClose() {}                                 // close video output file
  virtual void VideoWriteAudio( const short *pAudio, unsigned int uNumFrames ) {} // write some audio info
  virtual void VideoWriteImage( const unsigned char *pImage ) {} // write video image

  // ERROR FEEDBACK
  virtual void Error( const char * const msg ) {} // Called with explanation message if there was some sort of error in a plugin callback
};


class InternalsPluginV01 : public InternalsPlugin  // Version 01 is the exact same as the original
{
  // REMINDER: exported function GetPluginVersion() should return 1 if you are deriving from this InternalsPluginV01!
};


class InternalsPluginV02 : public InternalsPluginV01  // V02 contains everything from V01 plus the following:
{
  // REMINDER: exported function GetPluginVersion() should return 2 if you are deriving from this InternalsPluginV02!

 public:

  // VEHICLE CONTROL (PHYSICS/REPLAY)
  // To add vehicle, fill in data and return true.  You probably want to store the given ID so you can
  // correlate it with future callbacks.
  virtual bool WantsToAddVehicle( const long id, NewVehicleDataV01 &data ) { return( false ); }

  // InitVehicle() and ResetVehicle() are similar.  InitVehicle() is called only once upon vehicle load, while ResetVehicle()
  // is called immediately after InitVehicle() and then also whenever the vehicle is "reset" (user escapes back to the garage, or
  // the session changes, etc.).  ResetVehicle() must return a value indicating if the plugin wants to have some sort of control
  // over the vehicle.  These function also includes varied vehicle information which the plugin may wish to store, regardless of
  // whether it wants to control it.
  virtual void                  InitVehicle( const VehicleAndPhysicsV01 &data ) {}                       // called when vehicle is loaded
  virtual PluginControlRequest ResetVehicle( const VehicleAndPhysicsV01 &data ) { return( PCR_NONE ); }  // called when vehicle is reset
  virtual void UninitVehicle( const long id )                             {}                       // called when vehicle is unloaded

  // This function is called every time that the sim wants to set or reset the vehicle location (and stop vehicle).  This typically
  // happens right after the ResetVehicle() call, but it's possible to happen at other times.
  virtual void SetVehicleLocation( StartingVehicleLocationV01 &data ) {}

  // This essentially indicates that the engine starting procedure begin (the "instant" flag can be ignored; it is an aesthetic preference)
  virtual void StartVehicle( const long id, const bool instant ) {}

  // This function is called occasionally
  virtual void SetPhysicsOptions( PhysicsOptionsV01 &options ) {}

  // This is used to gather vehicle state override info for AI-controlled vehicles.  We will repeatedly call
  // this function, gathering any data points available for any vehicle, until it returns false.
  virtual bool GetVehicleState( VehicleStateV01 &data ) { return( false ); }

  // Use this function to apply additive forces to the default rFactor physics.  The force will be
  // applied at the vehicle's standardized CG (no fluids, wheels in default .PM positions), causing
  // very little or no torque.  The torque will be added to the body's other torques and the
  // engineTorque will be added directly to the current engine output.
  // NOTE: To use any physics functions in the future, this DLL will have to be specified in the HDV
  // as a physics plugin (for anti-cheat purposes).
  virtual bool AddPhysics( const long id, double et, PhysicsAdditiveV01 &add ) { return( false ); }

  // Tyre physics (only replaces tyre model in default rFactor physics)
  virtual void InitISOTyre( long id, ISOTyreInitV01 &init )                                       {}
  virtual bool ComputeISOTyreForces( long id, ISOTyreInputV01 &input, ISOTyreOutputV01 &output )  { return( false ); }

  // Differential physics (only replaces differential model in default rFactor physics)
  virtual void InitDifferential( long id, DifferentialInitV01 &init )                                              {}
  virtual bool ComputeDifferentialTransfer( long id, DifferentialInputV01 &input, DifferentialOutputV01 &output )  { return( false ); }

  // This will only be called if the HDV file designates this DLL as its physics plugin.
  // NOTE 2008.12.29: rFactor now calls RunPhysics() at exactly 400Hz regardless of the
  // returned GetPhysicsRate(), as long as it is a positive value.  In other words, if a
  // higher physics rate is desired, it should be implemented inside the plugin itself.
  // Also, only one plugin can receive RunPhysics() calls; rFactor arbitrarily chooses
  // one of the plugins that return the highest value for GetPhysicsRate().
  virtual long GetPhysicsRate( long id )                             { return( 0 ); } // now only used to activate RunPhysics() calls (and prioritize if multiple plugins return positive values); use 0 to disable plugin physics
  virtual void RunPhysics( long id, PhysicsInputV01 &input )         {} // run physics!
  virtual void GetPhysicsState( long id, PhysicsOutputV01 &output )  {} // output physics data
};


class InternalsPluginV03 : public InternalsPluginV02  // V03 contains everything from V02 plus the following:
{
  // REMINDER: exported function GetPluginVersion() should return 3 if you are deriving from this InternalsPluginV03!

 public:

  // RENDER CONTROL (Pre/Post draw)
  virtual long Init( void *pData ) { return( 0 ); }
  virtual long Post() { return( 0 ); }

  virtual long GetPreDrawParams( void *pData ) { return( 0 ); }
  virtual long SetPreDrawParams( void *pData ) { return( 0 ); }
  virtual long PreDraw() { return( 0 ); }

  virtual long GetPostDrawParams( void *pData ) { return( 0 ); }
  virtual long SetPostDrawParams( void *pData ) { return( 0 ); }
  virtual long PostDraw() { return( 0 ); }

  // EXTENDED VEHICLE CONTROL (PHYSICS/REPLAY)
  virtual bool WantsToDeleteVehicle( long &id )                       { return( false ); } // set ID and return true
  virtual bool WantsToViewVehicle( CameraControlInfoV01 &camControl ) { return( false ); } // set ID and camera type and return true

  // EXTENDED GAME OUTPUT
  virtual void UpdateGraphics( const GraphicsInfoV02 &info )          {} // update plugin with extended graphics info

  // MESSAGE BOX INPUT
  virtual bool WantsToDisplayMessage( MessageInfoV01 &msgInfo )       { return( false ); } // set message and return true
};


class InternalsPluginV04 : public InternalsPluginV03  // V04 contains everything from V03 plus the following:
{
  // REMINDER: exported function GetPluginVersion() should return 4 if you are deriving from this InternalsPluginV04!

 public:

  // EXTENDED VEHICLE CONTROL (PHYSICS/REPLAY)
  virtual bool WantsLocalGeometry()                                   { return( true );  } // wants geometry local to vehicle in realtime (presumably returns the opposite value of WantsFullGeometry())
  virtual bool WantsFullGeometry( unsigned long checksum )            { return( false ); } // wants full array of track geometry at loading time (checksum provided in case data is stored offline)
  virtual void SetFullGeometry( long numTriangles, PhysicsTriangleV01 *triangle )       {} // provides one massive array of triangles (plugin is responsible for realtime searching)

  // EXTENDED GAME FLOW NOTIFICATIONS
  virtual void SetEnvironment( const EnvironmentInfoV01 &info )       {} // may be called whenever the environment changes
};


//⁄ƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒø
//¿ƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒƒŸ

// See #pragma at top of file
#pragma pack( pop )

#endif _RF2_INTERNALS_PLUGIN_HPP_

