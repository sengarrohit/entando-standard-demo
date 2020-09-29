package org.entando.demo.banking.web.rest;

import org.entando.demo.banking.BankingApp;
import org.entando.demo.banking.config.TestSecurityConfiguration;
import org.entando.demo.banking.domain.Checkingtransaction;
import org.entando.demo.banking.repository.CheckingtransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link CheckingtransactionResource} REST controller.
 */
@SpringBootTest(classes = { BankingApp.class, TestSecurityConfiguration.class })
@AutoConfigureMockMvc
@WithMockUser
public class CheckingtransactionResourceIT {

    private static final LocalDate DEFAULT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(2);

    private static final BigDecimal DEFAULT_BALANCE = new BigDecimal(1);
    private static final BigDecimal UPDATED_BALANCE = new BigDecimal(2);

    private static final Long DEFAULT_ACCOUNT_ID = 1L;
    private static final Long UPDATED_ACCOUNT_ID = 2L;

    @Autowired
    private CheckingtransactionRepository checkingtransactionRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCheckingtransactionMockMvc;

    private Checkingtransaction checkingtransaction;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Checkingtransaction createEntity(EntityManager em) {
        Checkingtransaction checkingtransaction = new Checkingtransaction()
            .date(DEFAULT_DATE)
            .description(DEFAULT_DESCRIPTION)
            .amount(DEFAULT_AMOUNT)
            .balance(DEFAULT_BALANCE)
            .accountID(DEFAULT_ACCOUNT_ID);
        return checkingtransaction;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Checkingtransaction createUpdatedEntity(EntityManager em) {
        Checkingtransaction checkingtransaction = new Checkingtransaction()
            .date(UPDATED_DATE)
            .description(UPDATED_DESCRIPTION)
            .amount(UPDATED_AMOUNT)
            .balance(UPDATED_BALANCE)
            .accountID(UPDATED_ACCOUNT_ID);
        return checkingtransaction;
    }

    @BeforeEach
    public void initTest() {
        checkingtransaction = createEntity(em);
    }

    @Test
    @Transactional
    public void createCheckingtransaction() throws Exception {
        int databaseSizeBeforeCreate = checkingtransactionRepository.findAll().size();
        // Create the Checkingtransaction
        restCheckingtransactionMockMvc.perform(post("/api/checkingtransactions").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(checkingtransaction)))
            .andExpect(status().isCreated());

        // Validate the Checkingtransaction in the database
        List<Checkingtransaction> checkingtransactionList = checkingtransactionRepository.findAll();
        assertThat(checkingtransactionList).hasSize(databaseSizeBeforeCreate + 1);
        Checkingtransaction testCheckingtransaction = checkingtransactionList.get(checkingtransactionList.size() - 1);
        assertThat(testCheckingtransaction.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testCheckingtransaction.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testCheckingtransaction.getAmount()).isEqualTo(DEFAULT_AMOUNT);
        assertThat(testCheckingtransaction.getBalance()).isEqualTo(DEFAULT_BALANCE);
        assertThat(testCheckingtransaction.getAccountID()).isEqualTo(DEFAULT_ACCOUNT_ID);
    }

    @Test
    @Transactional
    public void createCheckingtransactionWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = checkingtransactionRepository.findAll().size();

        // Create the Checkingtransaction with an existing ID
        checkingtransaction.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCheckingtransactionMockMvc.perform(post("/api/checkingtransactions").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(checkingtransaction)))
            .andExpect(status().isBadRequest());

        // Validate the Checkingtransaction in the database
        List<Checkingtransaction> checkingtransactionList = checkingtransactionRepository.findAll();
        assertThat(checkingtransactionList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllCheckingtransactions() throws Exception {
        // Initialize the database
        checkingtransactionRepository.saveAndFlush(checkingtransaction);

        // Get all the checkingtransactionList
        restCheckingtransactionMockMvc.perform(get("/api/checkingtransactions?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(checkingtransaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(DEFAULT_AMOUNT.intValue())))
            .andExpect(jsonPath("$.[*].balance").value(hasItem(DEFAULT_BALANCE.intValue())))
            .andExpect(jsonPath("$.[*].accountID").value(hasItem(DEFAULT_ACCOUNT_ID.intValue())));
    }
    
    @Test
    @Transactional
    public void getCheckingtransaction() throws Exception {
        // Initialize the database
        checkingtransactionRepository.saveAndFlush(checkingtransaction);

        // Get the checkingtransaction
        restCheckingtransactionMockMvc.perform(get("/api/checkingtransactions/{id}", checkingtransaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(checkingtransaction.getId().intValue()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT.intValue()))
            .andExpect(jsonPath("$.balance").value(DEFAULT_BALANCE.intValue()))
            .andExpect(jsonPath("$.accountID").value(DEFAULT_ACCOUNT_ID.intValue()));
    }
    @Test
    @Transactional
    public void getNonExistingCheckingtransaction() throws Exception {
        // Get the checkingtransaction
        restCheckingtransactionMockMvc.perform(get("/api/checkingtransactions/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCheckingtransaction() throws Exception {
        // Initialize the database
        checkingtransactionRepository.saveAndFlush(checkingtransaction);

        int databaseSizeBeforeUpdate = checkingtransactionRepository.findAll().size();

        // Update the checkingtransaction
        Checkingtransaction updatedCheckingtransaction = checkingtransactionRepository.findById(checkingtransaction.getId()).get();
        // Disconnect from session so that the updates on updatedCheckingtransaction are not directly saved in db
        em.detach(updatedCheckingtransaction);
        updatedCheckingtransaction
            .date(UPDATED_DATE)
            .description(UPDATED_DESCRIPTION)
            .amount(UPDATED_AMOUNT)
            .balance(UPDATED_BALANCE)
            .accountID(UPDATED_ACCOUNT_ID);

        restCheckingtransactionMockMvc.perform(put("/api/checkingtransactions").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedCheckingtransaction)))
            .andExpect(status().isOk());

        // Validate the Checkingtransaction in the database
        List<Checkingtransaction> checkingtransactionList = checkingtransactionRepository.findAll();
        assertThat(checkingtransactionList).hasSize(databaseSizeBeforeUpdate);
        Checkingtransaction testCheckingtransaction = checkingtransactionList.get(checkingtransactionList.size() - 1);
        assertThat(testCheckingtransaction.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testCheckingtransaction.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testCheckingtransaction.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testCheckingtransaction.getBalance()).isEqualTo(UPDATED_BALANCE);
        assertThat(testCheckingtransaction.getAccountID()).isEqualTo(UPDATED_ACCOUNT_ID);
    }

    @Test
    @Transactional
    public void updateNonExistingCheckingtransaction() throws Exception {
        int databaseSizeBeforeUpdate = checkingtransactionRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCheckingtransactionMockMvc.perform(put("/api/checkingtransactions").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(checkingtransaction)))
            .andExpect(status().isBadRequest());

        // Validate the Checkingtransaction in the database
        List<Checkingtransaction> checkingtransactionList = checkingtransactionRepository.findAll();
        assertThat(checkingtransactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCheckingtransaction() throws Exception {
        // Initialize the database
        checkingtransactionRepository.saveAndFlush(checkingtransaction);

        int databaseSizeBeforeDelete = checkingtransactionRepository.findAll().size();

        // Delete the checkingtransaction
        restCheckingtransactionMockMvc.perform(delete("/api/checkingtransactions/{id}", checkingtransaction.getId()).with(csrf())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Checkingtransaction> checkingtransactionList = checkingtransactionRepository.findAll();
        assertThat(checkingtransactionList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
