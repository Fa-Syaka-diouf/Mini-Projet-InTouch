package com.elfstack.toys.admin.ui;

import com.elfstack.toys.admin.service.HolidaySyncService;
import com.elfstack.toys.security.dev.DevSecurityService;
import com.elfstack.toys.taskmanagement.ui.view.TaskFormView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Layout
public class AdminLayout extends AppLayout implements RouterLayout {
    private final DevSecurityService devSecurityService;
    public AdminLayout(HolidaySyncService holidaySyncService, DevSecurityService devSecurityService) {
        this.devSecurityService = devSecurityService;
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.getStyle()
                .set("background-color", "white")
                .set("border-bottom", "1px solid rgba(0, 0, 0, 0.1)")
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("width", "100%");

        Component userMenu = createUserMenu();
        var drawerToggle = new DrawerToggle();
        drawerToggle.addClassNames(LumoUtility.Margin.NONE);
        Div title = createHeader();
        var toggleAndTitle = new Div(drawerToggle, title);
        toggleAndTitle.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER);


        toolbar.add(toggleAndTitle, userMenu);
        addToNavbar(toolbar);
        var scroller = new Scroller(createSideNav());
        scroller.setClassName(LumoUtility.Padding.SMALL);
        scroller.getStyle().set("background-color","#243163");
        addToDrawer(scroller);
    }
    private Div createHeader() {
        var appLogo = new Image("images/logo.png", "Logo");
        appLogo.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.IconSize.LARGE);

        var appName = new Span("Page Admin");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);

        var header = new Div(appLogo, appName);
        header.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Padding.MEDIUM, LumoUtility.Gap.MEDIUM, LumoUtility.AlignItems.CENTER);
        return header;
    }
    private Component createUserMenu() {
        String user = devSecurityService.getCurrentUsername();
        var name = new Span (user);
        Icon icon_notif = LumoIcon.BELL.create();
        icon_notif.getStyle().set("color", "rgba(0,0,0,0.5)").set("margin-right", "3em");
        Avatar avatarBasic = new Avatar();
        avatarBasic.getStyle().set("color", "red").set("margin-right", "1em");

        var userMenu = new MenuBar();
        userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        userMenu.addClassNames(LumoUtility.Margin.MEDIUM);
        userMenu.addItem(icon_notif);
        var userMenuItem = userMenu.addItem(avatarBasic);
        userMenuItem.add(name);
        userMenuItem.getSubMenu().addItem("Déconnexion", event -> devSecurityService.logout());
        return userMenu;
    }
    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.getStyle().set("margin-top", "1rem");
        SideNavItem dashboard = new SideNavItem("Tableau de bord", AdminPage.class, new Icon(VaadinIcon.HOME));
        SideNavItem holiday = new SideNavItem("Jours Fériés", TaskFormView.class, new Icon(VaadinIcon.CALENDAR));
        SideNavItem Utilisateurs = new SideNavItem("Gestion des Utilisateurs", UserManagement.class, new Icon(VaadinIcon.USERS));
        SideNavItem Task_list = new SideNavItem("Liste des taches", TaskFormView.class, new Icon(VaadinIcon.CLIPBOARD_CHECK));
        SideNavItem Base_donne_holiday = new SideNavItem("Paramètre", AdminSettings.class, new Icon(VaadinIcon.COG));

        nav.addItem(dashboard, Task_list, holiday,Utilisateurs, Base_donne_holiday);
        return nav;
    }
}

