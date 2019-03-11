# SELECTive
## Info
No `database` files are in included in this distribution. The system is designed to be setup through a clean install. \
For this the system will require you to setup an initial admin. After this the system is ready for use. Files are mostly \
created when needed. \
\
**Important Information**:
1. The directory `/database` is automatically created at the project root directory
2. In case something has really gone wrong, deleting the directory `/database` will reset the system
3. If you have forgotten your admin details use the root to change your password ([root details](#Root-Details))
4. Deleting specific accounts, electives, or registrations is not supported in this version (apart from registering \
to another elective). If this must be done please delete the entry in the database directly. Be sure though to not \
new line characters, this will mess things up
5. Generally every entry is stored in the form of: `id ;; property1 ;; property2 ;; ... ;; propertyn \n` \
apart from the authentication which is stored as: `username hash : password hash : USERTYPE \n`

## Root Details
`username` = `sudo` \
`password` = `masterPAss_2k19` \
_Or check the code as well for the details_