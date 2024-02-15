package com.bundee.ums.defs;

public class UMSDefs {
    public class Endpoints {
        public static final String CREATE_USERS = "api/v1/user/createUser";
        public static final String CHECK_LOGIN = "api/v1/user/checkLogin";
        public static final String COUNT_BY_ID = "api/v1/user/getUserById";
        public static final String COUNT_BY_USERIDS = "api/v1/user/getUserByUserIds";
        public static final String GET_BY_FIREBASE_IDS = "api/v1/user/getUserByFirebaseId";
        public static final String EMAIL_VERIFICATION = "api/v1/user/getUserByEmail";
        public static final String UPDATE_USER = "api/v1/user/updateUser";
        public static final String UPDATE_USER_TOKEN = "api/v1/user/updateUserCustomerToken";
        public static final String ALL_USERS = "api/v1/user/getAllUsers";
        public static final String INSERT_INTO_PUSHNOTIFICATIONS = "api/v1/user/insertPushNotification";
        public static final String GET_ALL_PUSHNOTIFICXATIONS = "api/v1/user/getAllPushNotification";
        public static final String GET_PUSHNOTIFICATIONS_BY_ID = "api/v1/user/getPushNotificationById";
        public static final String UPDATE_PUSHNOTIFICATIONS = "api/v1/user/updatePushNotification";
        public static final String UPDATE_PUSHnOTIFICATION_DEVICETOKEN_BYUSERID = "api/v1/user/updatePushNotifications";
        public static final String INSERT_INTO_OWNEREMPLOYEE = "api/v1/user/insertOwnerEmployeeMapping";
        public static final String GET_ALL_OWNEREMPLOYEEMAPPING = "api/v1/user/getAllOwnerEmployeeMapping";
        public static final String GET_OWNEREMPLOYEE_BY_ID = "api/v1/user/getOwnerEmployeeMappingById";
        public static final String UPDATE_OWNER_EMPLOYEE = "api/v1/user/updateOwnerEmployeeMapping";
        public static final String GET_ALL_MASTER_ROLES = "api/v1/user/getAllMasterRoles";
        public static final String GET_MASTER_ROLES_BYID = "api/v1/user/getMasterRoleById";
        public static final String GET_MASTER_ROLES_BY_NAME = "api/v1/user/getMasterRoleByName";
        public static final String ADD_MASTER_ROLES = "api/v1/user/insertMasterRoles";
        public static final String UPDATE_MASTER_ROLES = "api/v1/user/updateMasterRoles";
        public static final String GET_ALL_USER_ROLE_MAPPING = "api/v1/user/getAllUserRoleMapping";
        public static final String GET_ALL_USER_ROLE_MAPPING_BYID = "api/v1/user/userRoleMappingById";
        public static final String ADD_USER_ROLE_MAPPING = "api/v1/user/addUserRoleMapping";
        public static final String UPDATE_USER_ROLE_MAPPING = "api/v1/user/updateUserRoleMapping";
        public static final String GET_ALL_MOBILECONF = "api/v1/user/getAllMobileConfiguration";
        public static final String GET_ALL_MOBILE_CONF_BY_ID = "api/v1/user/getAllConfigurationById";
        public static final String INSERT_MOBILE_DETAILS = "api/v1/user/insertMobileDetails";
        public static final String UPDATE_MOBILE_DETAILS = "api/v1/user/updateMobileDetails";
        public static final String LOGIN_USER = "api/v1/user/login";
        public static final String VALIDATE_USER_AUTH_TOK = "api/v1/user/token/validate";
        public static final String USER_PERMISSION_TEST = "api/v1/user/permission/test";
        public static final String CREATE_NEW_API_KEY = "api/v1/apikey/create";
        public static final String LIST_ALL_API_KEY = "api/v1/apikey/list";
        public static final String ENABLE_API_KEY = "api/v1/apikey/enable";
        public static final String DISABLE_API_KEY = "api/v1/apikey/disable";
        public static final String ASSIGN_ROLES_TO_API_KEY = "api/v1/apikey/roles/assign";
        public static final String GEN_API_KEY_TOKEN = "api/v1/apikey/token/gen";
        public static final String GET_API_VERSION = "api/v1/user/getAppVersion";
        public static final String INSERT_ERROR_LOG = "api/v1/user/insertError";
        public static final String CREATE_DRIVER_PROFILE = "api/v1/user/createDriverProfile";
        public static final String GET_DRIVER_PROFILE = "api/v1/user/getDriverProfile";
        public static final String UPDATE_DRIVER_PROFILE = "api/v1/user/updateDriverProfile";
        public static final String INSERT_HOST_PROFILE = "api/v1/user/insertHostProfile";
        public static final String HOST_PROFILE = "api/v1/user/getHostProfile";
        public static final String ONBOARD_HOST = "api/v1/user/hostOnBoard";

    }

    public class Permissions {
        public static final String CREATE_NEW_API_KEY = "CREATE_NEW_API_KEY";
        public static final String LIST_ALL_API_KEY = "LIST_ALL_API_KEY";
        public static final String OPEN_API_KEY = "OPEN_API_KEY";
        public static final String ADMIN_API_KEY = "ADMIN_API_KEY";
        public static final String USER_API_KEY = "USER_API_KEY";
        public static final String USER_N_HOST_API_KEY = "USER_N_HOST_API_KEY";
        public static final String HOST_API_KEY = "HOST_API_KEY";
        public static final String ENABLE_API_KEY = "ENABLE_API_KEY";
        public static final String DISABLE_API_KEY = "DISABLE_API_KEY";
        public static final String ASSIGN_ROLES_TO_API_KEY = "ASSIGN_ROLES_TO_API_KEY";
        public static final String GEN_API_KEY_TOKEN = "GEN_API_KEY_TOKEN";

    }
}
