package npetzall.httpdouble.admin.registry;

import npetzall.httpdouble.admin.services.AdminService;

public class AdminServiceRef {
    private String name;
    private AdminService adminService;

    public AdminServiceRef(String name, AdminService adminService) {
        this.name = name;
        this.adminService = adminService;
    }

    public String getName() {
        return name;
    }

    public AdminService getAdminService() {
        return adminService;
    }
}
