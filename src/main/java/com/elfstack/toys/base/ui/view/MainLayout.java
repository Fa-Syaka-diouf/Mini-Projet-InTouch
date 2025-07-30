package com.elfstack.toys.base.ui.view;

import com.elfstack.toys.admin.service.HolidaySyncService;
import com.elfstack.toys.admin.ui.AdminJourFerie;
import com.elfstack.toys.admin.ui.AdminPage;
import com.elfstack.toys.security.CurrentUser;
import com.elfstack.toys.security.dev.DevLogoutView;
import com.elfstack.toys.security.dev.DevSecurityService;
import com.elfstack.toys.taskmanagement.ui.view.TaskListView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import static com.vaadin.flow.theme.lumo.LumoUtility.*;

public final class MainLayout extends AppLayout {

    private final DevSecurityService devSecurityService;
    public MainLayout(HolidaySyncService holidaySyncService, DevSecurityService devSecurityService) {
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
        scroller.getStyle().set("background-color","#243163");
        scroller.setClassName(LumoUtility.Padding.SMALL);
        addToDrawer(scroller);
    }
    private Div createHeader() {
        var appLogo = new Image("images/logo.png", "Logo");
        appLogo.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.IconSize.LARGE);

        var appName = new Span("InTouchTask");
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
        SideNavItem dashboard = new SideNavItem("Tableau de bord", MainView.class, new Icon(VaadinIcon.HOME));
        SideNavItem mesTaches = new SideNavItem("Mes Tâches", MainTask.class, new Icon(VaadinIcon.CLIPBOARD_CHECK));
        SideNavItem notifications = new SideNavItem("Notifications", MainNotification.class, new Icon(VaadinIcon.BELL));
        SideNavItem historique = new SideNavItem("Historique", MainHistorique.class, new Icon(VaadinIcon.ARCHIVE));

        nav.addItem(dashboard, mesTaches, notifications, historique);

        if (devSecurityService.isAdmin()) {
            SideNavItem pageAdmin = new SideNavItem("Page Admin", AdminPage.class, new Icon(VaadinIcon.TOOLS));
            nav.addItem(pageAdmin);
        }


        return nav;
    }

}
