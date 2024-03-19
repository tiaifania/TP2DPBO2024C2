import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Menu extends JFrame{
    public static void main(String[] args) {
        // buat object window
        Menu window = new Menu();


        // atur ukuran window
        window.setSize(480, 560);

        // letakkan window di tengah layar
        window.setLocationRelativeTo(null);
        // isi window
        window.setContentPane(window.mainPanel);

        // ubah warna background
        window.getContentPane().setBackground(Color.RED);

        // tampilkan window
        window.setVisible(true);

        // agar program ikut berhenti saat window diclose
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    // index baris yang diklik
    private int selectedIndex = -1;
    // list untuk menampung semua mahasiswa
    private ArrayList<Mahasiswa> listMahasiswa;

    private  Database database;
    private JPanel mainPanel;
    private JTextField nimField;
    private JTextField namaField;
    private JTable mahasiswaTable;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JComboBox jenisKelaminComboBox;
    private JButton deleteButton;
    private JLabel titleLabel;
    private JLabel nimLabel;
    private JLabel namaLabel;
    private JLabel jenisKelaminLabel;
    private JComboBox jalurMasukcomboBox;
    private JLabel jalurMasuk;

    // constructor
    public Menu() {
        // inisialisasi listMahasiswa
        listMahasiswa = new ArrayList<>();

        // buat object database
        database = new Database();

        // isi tabel mahasiswa
        mahasiswaTable.setModel(setTable());

        // ubah styling title
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));

        // atur isi combo box
        String[] jenisKelaminData = {"", "Laki-laki", "Perempuan"};
        jenisKelaminComboBox.setModel(new DefaultComboBoxModel(jenisKelaminData));

        String[] jalurMasukData = {"", "SNMPTN", "SBMPTN", "MANDIRI"};
        jalurMasukcomboBox.setModel(new DefaultComboBoxModel(jalurMasukData));


        // sembunyikan button delete
        deleteButton.setVisible(false);

        // saat tombol add/update ditekan
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex == -1){
                    insertData();
                } else {
                    updateData();
                }




            }
        });
        // saat tombol delete ditekan
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex >= 0) {
                    deleteData();
                }


            }
        });
        // saat tombol cancel ditekan
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });
        // saat salah satu baris tabel ditekan
        mahasiswaTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // ubah selectedIndex menjadi baris tabel yang diklik
                selectedIndex = mahasiswaTable.getSelectedRow();

                // simpan value textfield dan combo box
                String selectedNim = mahasiswaTable.getModel().getValueAt(selectedIndex, 1).toString();
                String selectedNama = mahasiswaTable.getModel().getValueAt(selectedIndex, 2).toString();
                String selectedJenisKelamin = mahasiswaTable.getModel().getValueAt(selectedIndex, 3).toString();
                String selectedJalurMasuk = mahasiswaTable.getModel().getValueAt(selectedIndex, 4).toString();




                // ubah isi textfield dan combo box
                nimField.setText(selectedNim);
                namaField.setText(selectedNama);
                jenisKelaminComboBox.setSelectedItem(selectedJenisKelamin);
                jalurMasukcomboBox.setSelectedItem(selectedJalurMasuk);



                // ubah button "Add" menjadi "Update"
                addUpdateButton.setText("Update");

                // tampilkan button delete
                deleteButton.setVisible(true);
            }
        });
    }

    public final DefaultTableModel setTable() {
        // tentukan kolom tabel
        Object[] column = {"No", "NIM", "Nama", "Jenis Kelamin", "Jalur Masuk"};

        // buat objek tabel dengan kolom yang sudah dibuat
        DefaultTableModel temp = new DefaultTableModel(null, column);

        try {
            ResultSet resultSet = database.selectQuery("SELECT * FROM mahasiswa");

            int i = 0;
            while (resultSet.next()) {
                Object[] row = new Object[5];
                row[0] = i + 1;
                row[1] = resultSet.getString("nim");
                row[2] = resultSet.getString("nama");
                row[3] = resultSet.getString("jenis_kelamin");
                row[4] = resultSet.getString("jalur_masuk");

                temp.addRow(row);
                i++;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return temp; // return juga harus diganti
    }

    public void insertData() {
        // ambil value dari textfield dan combobox
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();
        String jalurMasuk = jalurMasukcomboBox.getSelectedItem().toString();

        if (nim.isEmpty() || nama.isEmpty() || jenisKelamin.isEmpty() || jalurMasuk.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Harap isi semua kolom.");
            return;
        }
        // tambahkan data ke dalam database
        //listMahasiswa.add(new Mahasiswa(nim, nama, jenisKelamin, jalurMasuk));
        //String oldNim = mahasiswaTable.getModel().getValueAt(selectedIndex, 1).toString().trim();

        // Cek apakah NIM yang akan diupdate sudah ada di dalam database, kecuali jika NIM tersebut adalah NIM lama (NIM sebelum diedit)
        if (isNIMExists(nim)) {
            JOptionPane.showMessageDialog(null, "NIM tidak boleh sama.");
            return; // Jika NIM sudah ada, hentikan proses update
        }

        String sql = "INSERT INTO mahasiswa VALUES (null, '" + nim +"', '" + nama + "', '" + jenisKelamin + "', '" + jalurMasuk + "');";
        database.insertUpdateDeleteQuery(sql);


        // update tabel
        mahasiswaTable.setModel(setTable());


        // bersihkan form
        clearForm();


        // feedback
        System.out.println("Insert berhasil!");
        JOptionPane.showMessageDialog(null, "DAta berhasil ditambahkan");



    }

    private boolean isNIMExists(String nim) {
        try {
            ResultSet resultSet = database.selectQuery("SELECT * FROM mahasiswa WHERE nim = '" + nim + "'");
            return resultSet.next(); // Jika resultSet memiliki baris, berarti NIM sudah ada di database
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void updateData() {
        // ambil data dari form
        String nim = nimField.getText().trim();
        String nama = namaField.getText().trim();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString().trim();
        String jalurMasuk = jalurMasukcomboBox.getSelectedItem().toString().trim();

        if (nim.isEmpty() || nama.isEmpty() || jenisKelamin.isEmpty() || jalurMasuk.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Harap isi semua kolom.");
            return;
        }

        String oldNim = mahasiswaTable.getModel().getValueAt(selectedIndex, 1).toString().trim();

        // Cek apakah NIM yang akan diupdate sudah ada di dalam database, kecuali jika NIM tersebut adalah NIM lama (NIM sebelum diedit)
        if (isNIMExists(nim) && !oldNim.equals(nim)) {
            JOptionPane.showMessageDialog(null, "NIM tidak boleh sama.");
            return; // Jika NIM sudah ada, hentikan proses update
        }



        // Jalankan perintah SQL untuk update data di database
        String sql = "UPDATE mahasiswa SET nama = '" + nama + "', jenis_kelamin = '" + jenisKelamin + "', jalur_masuk = '" + jalurMasuk + "' WHERE nim = '" + nim + "'";
        database.insertUpdateDeleteQuery(sql);

        // Perbarui data di dalam tabel
        DefaultTableModel model = (DefaultTableModel) mahasiswaTable.getModel();
        model.removeRow(selectedIndex); // Hapus baris lama yang telah diubah
        Object[] newRow = {selectedIndex + 1, nim, nama, jenisKelamin, jalurMasuk}; // Data baris baru
        model.insertRow(selectedIndex, newRow); // Tambahkan baris baru ke posisi yang tepat

        // Update selectedIndex
        selectedIndex--;

        // Bersihkan form
        clearForm();

        // Feedback
        System.out.println("Update Berhasil!");
        JOptionPane.showMessageDialog(null, "Data berhasil diubah!");



    }

    public void deleteData() {
        // Tampilkan dialog konfirmasi
        int option = JOptionPane.showConfirmDialog(null, "Hapus data?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            // Hapus data dari list
            //listMahasiswa.remove(selectedIndex);

            // Ambil NIM dari data yang dipilih
            String nim = mahasiswaTable.getModel().getValueAt(selectedIndex, 1).toString();

            // Jalankan perintah SQL untuk menghapus data dari database
            String sql = "DELETE FROM mahasiswa WHERE nim = '" + nim + "'";
            database.insertUpdateDeleteQuery(sql);

            // Update tabel
            mahasiswaTable.setModel(setTable());

            // Bersihkan form
            clearForm();

            // Feedback
            System.out.println("Delete Berhasil!");
            JOptionPane.showMessageDialog(null, "Data berhasil dihapus!");
        }
    }

    public void clearForm() {
        // kosongkan semua texfield dan combo box
        nimField.setText("");
        namaField.setText("");
        jenisKelaminComboBox.setSelectedItem("");
        jalurMasukcomboBox.setSelectedItem("");



        // ubah button "Update" menjadi "Add"
        addUpdateButton.setText("Add");
        // sembunyikan button delete
        deleteButton.setVisible(false);
        // ubah selectedIndex menjadi -1 (tidak ada baris yang dipilih)
        selectedIndex = -1;
    }


}
