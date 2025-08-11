package com.elfstack.toys.base.ui.view;

import com.elfstack.toys.admin.service.HolidaySyncService;
import com.elfstack.toys.admin.ui.AdminJourFerie;
import com.elfstack.toys.admin.ui.AdminPage;
import com.elfstack.toys.security.CurrentUser;
import com.elfstack.toys.security.dev.DevLogoutView;
import com.elfstack.toys.security.dev.DevSecurityService;
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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
        drawerToggle.getStyle().setColor("#243163");
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
        var appLogo = new Image("logo.png", "Logo");
        appLogo.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.IconSize.LARGE);

        var appName = new Span("InTouchTask");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);

        var header = new Div(appLogo, appName);
        header.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Padding.MEDIUM, LumoUtility.Gap.MEDIUM, LumoUtility.AlignItems.CENTER);
        return header;
    }
    private Component createUserMenu() {
        String user = devSecurityService.getCurrentFullname();

        var userMenuContainer = new HorizontalLayout();
        userMenuContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        userMenuContainer.setSpacing(true);
        userMenuContainer.setPadding(false);
        userMenuContainer.addClassNames(
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.BorderRadius.MEDIUM
        );

        var notificationWrapper = new Div();
        notificationWrapper.addClassNames(LumoUtility.Position.RELATIVE);

        Icon iconNotif = LumoIcon.BELL.create();
        iconNotif.addClassNames(LumoUtility.IconSize.MEDIUM);
        iconNotif.getStyle()
                .set("color", "#243163")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease")
                .set("border-radius", "50%");

        var notificationBadge = new Span("3");
        notificationBadge.addClassNames(
                LumoUtility.FontSize.XSMALL,
                LumoUtility.TextColor.PRIMARY_CONTRAST
        );
        notificationBadge.getStyle()
                .set("position", "absolute")
                .set("top", "-2px")
                .set("right", "-6px")
                .set("background", "var(--lumo-error-color)")
                .set("color", "white")
                .set("border-radius", "50%")
                .set("width", "15px")
                .set("height", "15px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-weight", "bold")
                .set("font-size", "10px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.2)");

        notificationWrapper.add(iconNotif, notificationBadge);

        iconNotif.getElement().addEventListener("mouseenter", e -> {
            iconNotif.getStyle()
                    .set("background", "rgba(var(--lumo-primary-color-rgb), 0.1)")
                    .set("color", "var(--lumo-primary-color)")
                    .set("transform", "scale(1.1)");
        });

        iconNotif.getElement().addEventListener("mouseleave", e -> {
            iconNotif.getStyle()
                    .set("background", "transparent")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("transform", "scale(1)");
        });

        Avatar avatar = new Avatar();
        avatar.setName(user);
        avatar.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderColor.PRIMARY_10);
        avatar.getStyle()
                .set("background", "#243163")
                .set("color", "white")
                .set("font-weight", "600")
                .set("box-shadow", "0 4px 12px rgba(var(--lumo-primary-color-rgb), 0.3)")
                .set("transition", "all 0.3s ease")
                .set("cursor", "pointer");

        var userName = new Span(user);
        userName.addClassNames(
                LumoUtility.FontWeight.SEMIBOLD,
                LumoUtility.TextColor.BODY,
                LumoUtility.FontSize.SMALL
        );
        userName.getStyle()
                .set("margin-left", "8px")
                .set("margin-right", "4px");

        Icon dropdownIcon = LumoIcon.DROPDOWN.create();
        dropdownIcon.addClassNames(LumoUtility.IconSize.SMALL);
        dropdownIcon.getStyle()
                .set("color", "var(--lumo-tertiary-text-color)")
                .set("transition", "transform 0.2s ease");

        var userMenu = new MenuBar();
        userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        userMenu.getStyle()
                .set("background", "transparent")
                .set("border-radius", "12px")
                .set("transition", "all 0.2s ease");

        var userItemContainer = new HorizontalLayout();
        userItemContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        userItemContainer.setSpacing(false);
        userItemContainer.setPadding(false);
        userItemContainer.add(avatar, userName, dropdownIcon);

        userItemContainer.getStyle()
                .set("padding", "6px 12px")
                .set("border-radius", "8px")
                .set("transition", "all 0.2s ease")
                .set("cursor", "pointer");

        userItemContainer.getElement().addEventListener("mouseenter", e -> {
            userItemContainer.getStyle()
                    .set("background", "rgba(var(--lumo-primary-color-rgb), 0.08)");
            avatar.getStyle()
                    .set("transform", "scale(1.05)")
                    .set("box-shadow", "0 6px 16px rgba(var(--lumo-primary-color-rgb), 0.4)");
            dropdownIcon.getStyle()
                    .set("transform", "rotate(180deg)")
                    .set("color", "var(--lumo-primary-color)");
        });

        userItemContainer.getElement().addEventListener("mouseleave", e -> {
            userItemContainer.getStyle()
                    .set("background", "transparent");
            avatar.getStyle()
                    .set("transform", "scale(1)")
                    .set("box-shadow", "0 4px 12px rgba(var(--lumo-primary-color-rgb), 0.3)");
            dropdownIcon.getStyle()
                    .set("transform", "rotate(0deg)")
                    .set("color", "var(--lumo-tertiary-text-color)");
        });

        var userMenuItem = userMenu.addItem(userItemContainer);
        var subMenu = userMenuItem.getSubMenu();
        Icon profileIcon = LumoIcon.USER.create();
        profileIcon.addClassNames(LumoUtility.IconSize.SMALL);
        profileIcon.getStyle().set("margin-right", "8px").set("color", "var(--lumo-primary-color)");

        var profileItem = new HorizontalLayout();
        profileItem.setAlignItems(FlexComponent.Alignment.CENTER);
        profileItem.add(profileIcon, new Span("Mon Profil"));
        profileItem.setPadding(false);
        profileItem.setSpacing(false);

        subMenu.addItem(profileItem, event -> {
            Notification.show("Redirection vers le profil", 2000, Notification.Position.TOP_CENTER);
        });

        Icon settingsIcon = LumoIcon.COG.create();
        settingsIcon.addClassNames(LumoUtility.IconSize.SMALL);
        settingsIcon.getStyle().set("margin-right", "8px").set("color", "var(--lumo-contrast-60pct)");

        var settingsItem = new HorizontalLayout();
        settingsItem.setAlignItems(FlexComponent.Alignment.CENTER);
        settingsItem.add(settingsIcon, new Span("Paramètres"));
        settingsItem.setPadding(false);
        settingsItem.setSpacing(false);

        subMenu.addItem(settingsItem, event -> {
            Notification.show("Redirection vers les paramètres", 2000, Notification.Position.TOP_CENTER);
        });
        subMenu.addSeparator();
        Icon logoutIcon = LumoIcon.ARROW_RIGHT.create();
        logoutIcon.addClassNames(LumoUtility.IconSize.SMALL);
        logoutIcon.getStyle().set("margin-right", "8px").set("color", "var(--lumo-error-color)");

        var logoutItem = new HorizontalLayout();
        logoutItem.setAlignItems(FlexComponent.Alignment.CENTER);
        logoutItem.add(logoutIcon, new Span("Déconnexion"));
        logoutItem.setPadding(false);
        logoutItem.setSpacing(false);
        logoutItem.getStyle().set("color", "var(--lumo-error-color)");

        subMenu.addItem(logoutItem, event -> devSecurityService.logout());
        userMenuContainer.add(notificationWrapper, userMenu);

        return userMenuContainer;
    }
    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.getStyle().set("margin-top", "1rem");
        SideNavItem dashboard = new SideNavItem("Mes Tâches", MainView.class, new Icon(VaadinIcon.CLIPBOARD_CHECK));
        SideNavItem notifications = new SideNavItem("Notifications", MainNotification.class, new Icon(VaadinIcon.BELL));
        SideNavItem historique = new SideNavItem("Historique", MainHistorique.class, new Icon(VaadinIcon.ARCHIVE));

        nav.addItem(dashboard, notifications, historique);

        if (devSecurityService.isAdmin()) {
            SideNavItem pageAdmin = new SideNavItem("Page Admin", AdminPage.class, new Icon(VaadinIcon.TOOLS));
            nav.addItem(pageAdmin);
        }


        return nav;
    }

}
