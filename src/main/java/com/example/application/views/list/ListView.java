package com.example.application.views.list;

import com.example.application.data.entity.Contact;
import com.example.application.data.service.CrmService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "")
@PageTitle("Java CRM Project")
public class ListView extends VerticalLayout { // The view extends VerticalLayout, which places all child components vertically
    Grid<Contact> grid = new Grid<>(Contact.class); // The Grid component is typed with Contact
    TextField filterText = new TextField();
    ContactForm form; // Creates a field for the form, so you have access to it from other methods later on
    CrmService service;

    public ListView(CrmService service) {
        this.service = service;
        addClassName("list-view");
        setSizeFull();
        configureGrid(); // The grid configuration is extracted to a separate method to keep the constructor easier to read
        //configureForm(); // Create a method for initializing the form

        form = new ContactForm(service.findAllCompanies(), service.findAllStatuses());
        form.setWidth("25em");
        form.addListener(ContactForm.SaveEvent.class, this::saveContact);
        form.addListener(ContactForm.DeleteEvent.class, this::deleteContact);
        form.addListener(ContactForm.CloseEvent.class, e -> closeEditor());

        FlexLayout content = new FlexLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.setFlexShrink(0, form);
        content.addClassNames("content", "gap-m");
        content.setSizeFull();

        add(
                getToolbar(),
                content); // Change the add() method to call getContent(). The method returns a HorizontalLayout
        // that wraps the form and the grid, showing them next to each other
        updateList();
        closeEditor();
        grid.asSingleSelect().addValueChangeListener(event ->
                editContact(event.getValue()));
    }

    private void configureGrid() {
        grid.addClassNames("contact-grid");
        grid.setSizeFull();
        grid.setColumns("firstName", "lastName", "email"); // Define which properties of Contact the grid should show
        grid.addColumn(contact -> contact.getStatus().getName()).setHeader("Status"); // Define custom columns for nested objects
        grid.addColumn(contact -> contact.getCompany().getName()).setHeader("Company");
        grid.getColumns().forEach(col -> col.setAutoWidth(true)); // Configure the columns to automatically adjust their
        // size to fit their contents
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);  // Configure the search field to fire value-change events
        // only when the user stops typing. This way you avoid
        // unnecessary database calls
        filterText.addValueChangeListener(e -> updateList());
        Button addContactButton = new Button("Add contact");
        addContactButton.addClickListener(click -> addContact());
        HorizontalLayout toolbar = new HorizontalLayout(filterText, addContactButton); // The toolbar uses a
        // HorizontalLayout to place the
        // TextField and Button next to each other.
        toolbar.addClassName("toolbar");
        return toolbar;
    }
    private void saveContact(ContactForm.SaveEvent event) {
        service.saveContact(event.getContact());
        updateList();
        closeEditor();
    }

    private void deleteContact(ContactForm.DeleteEvent event) {
        service.deleteContact(event.getContact());
        updateList();
        closeEditor();
    }
    public void editContact(Contact contact) {
        if (contact == null) {
            closeEditor();
        } else {
            form.setContact(contact);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void closeEditor() {
        form.setContact(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void addContact() {
        grid.asSingleSelect().clear();
        editContact(new Contact());
    }

    private void updateList() {
        grid.setItems(service.findAllContacts(filterText.getValue()));
    }
}