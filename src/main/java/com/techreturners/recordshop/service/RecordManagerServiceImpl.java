package com.techreturners.recordshop.service;

import com.techreturners.recordshop.exception.InvalidRecordInputException;
import com.techreturners.recordshop.exception.RecordAlreadyExistsException;
import com.techreturners.recordshop.exception.RecordNotFoundException;
import com.techreturners.recordshop.model.MusicGenre;
import com.techreturners.recordshop.model.MusicRecord;
import com.techreturners.recordshop.repository.RecordManagerRepository;
import com.techreturners.recordshop.validator.MusicRecordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class RecordManagerServiceImpl implements RecordManagerService {

    @Autowired
    RecordManagerRepository musicRecordManagerRepository;

    @Override
    public MusicRecord insertMusicRecord(MusicRecord musicRecord) {
        String albumName = musicRecord.getAlbumName();

        if (albumName != null) {
            Optional<MusicRecord> recordOptional =
                    musicRecordManagerRepository.findByAlbumName(albumName);
            if (recordOptional.isPresent()) {
                throw new RecordAlreadyExistsException("Album with name: " +
                        musicRecord.getAlbumName() +
                        " already exists and cannot be inserted again");
            }
        }
        boolean isValidReleaseYear = MusicRecordValidator.validateReleaseYear(musicRecord.getReleaseYear());
        boolean isValidStock = MusicRecordValidator.validateStock(musicRecord.getStock());
        if (isValidReleaseYear && isValidStock)
            return musicRecordManagerRepository.save(musicRecord);
        else
            throw new InvalidRecordInputException("Invalid input entered. Please enter" +
                    "only integers");
    }

    @Override
    public List<MusicRecord> getMusicRecordByReleaseYear(Integer releaseYear) {

        if (releaseYear != null) {
            Optional<List<MusicRecord>> musicRecordOptionalList =
                    musicRecordManagerRepository.findByReleaseYear(releaseYear);
            if (musicRecordOptionalList.isPresent()) {
                return musicRecordOptionalList.get();
            }
        }
        throw new RecordNotFoundException("Record with release year: " +
                releaseYear + " is not found");
    }

    @Override
    public boolean deleteRecordById(Long recordId) {
        if (recordId != null) {
            Optional<MusicRecord> musicRecordOptional =
                    musicRecordManagerRepository.findById(recordId);
            if (musicRecordOptional.isPresent()) {
                musicRecordManagerRepository.deleteById(recordId);
                return true;
            }
        }
        throw new RecordNotFoundException("Music Record with record Id: " +
                recordId + " is not found for delete");
    }

    @Override
    public boolean updateStockAmount(Long recordId, Long stock) {
        if (recordId != null && stock != null) {
            Optional<MusicRecord> musicRecordOptional = musicRecordManagerRepository.findById(recordId);
            if (musicRecordOptional.isPresent()) {
                MusicRecord musicRecord = musicRecordOptional.get();
                musicRecord.setStock(stock);
                musicRecordManagerRepository.save(musicRecord);
                return true;
            }
        }
        throw new RecordNotFoundException("Music Record with record Id: " +
                recordId + " is not found for update");
    }

    @Override
    public List<MusicRecord> getAllAlbumsByArtist(String artistName) {
        if (artistName != null) {
            Optional<List<MusicRecord>> allAlbumnsOptional =
                    musicRecordManagerRepository.findAllByArtist(artistName);
            if (allAlbumnsOptional.isPresent()) {
                return allAlbumnsOptional.get();
            }
        }
        throw new RecordNotFoundException("Cannot find Music Records for artist: "
                + artistName);
    }

    @Override
    public List<MusicRecord> getAllRecordsInStock() {
        return StreamSupport.stream(musicRecordManagerRepository.findAll().spliterator(), false)
                .filter(record -> record.getStock() > 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<MusicRecord> getAllRecordsByGenre(MusicGenre genre) {
        return musicRecordManagerRepository.findByGenre(genre);
    }

    @Override
    public MusicRecord replaceExistingRecord(Long id, MusicRecord record) {
//        Optional<MusicRecord> existingRecord = musicRecordManagerRepository.findById(id);
//        if(existingRecord.isPresent()){
//            MusicRecord recordToBeReplaced = existingRecord.get();
//            recordToBeReplaced.setAlbumName(record.getAlbumName());
//            recordToBeReplaced.setArtist(record.getArtist());
//            recordToBeReplaced.setReleaseYear(record.getReleaseYear());
//            recordToBeReplaced.setStock(record.getStock());
//            recordToBeReplaced.setGenre(record.getGenre());
//            return musicRecordManagerRepository.save(recordToBeReplaced);
//        }else {
//            throw new RecordNotFoundException("Music Record not found with ID: " + id);
//        }
        MusicRecord existingRecord = musicRecordManagerRepository.findById(id).orElseThrow(() -> new RecordNotFoundException("Product not found with id: " + id));
        existingRecord.setAlbumName(record.getAlbumName());
        existingRecord.setArtist(record.getArtist());
        existingRecord.setReleaseYear(record.getReleaseYear());
        existingRecord.setStock(record.getStock());
        existingRecord.setGenre(record.getGenre());
        return musicRecordManagerRepository.save(existingRecord);
    }

}
