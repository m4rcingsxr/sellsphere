package com.sellsphere.admin.mainpage;

import com.sellsphere.admin.FileService;
import com.sellsphere.common.entity.Article;
import com.sellsphere.common.entity.CarouselImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CarouselImageService {

    private final CarouselImageRepository carouselImageRepository;
    private static final String FOLDER_NAME = "main/carousel";

    /**
     * Retrieves all carousel images from the repository.
     *
     * @return a list of all carousel images
     */
    public List<CarouselImage> findAll() {
        return carouselImageRepository.findAll();
    }

    /**
     * Saves a carousel image associated with a specific article.
     *
     * @param file    the uploaded image file
     * @param article the associated article for the image
     * @throws IOException if there's an error saving the file
     */
    public void saveImage(MultipartFile file, Article article) throws IOException {
        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            FileService.saveFile(file, FOLDER_NAME, fileName);

            CarouselImage carouselImage = new CarouselImage(fileName);
            carouselImage.setArticle(article);

            carouselImageRepository.save(carouselImage);
        }
    }

    /**
     * Deletes a carousel image by its ID, removing the file from storage.
     *
     * @param id the ID of the carousel image to delete
     * @throws CarouselItemNotFoundException if the image isn't found
     */
    public void delete(Integer id) throws CarouselItemNotFoundException {
        CarouselImage carouselImage = carouselImageRepository
                .findById(id)
                .orElseThrow(CarouselItemNotFoundException::new);

        FileService.removeFile(FOLDER_NAME + "/" + carouselImage.getName());
        carouselImageRepository.delete(carouselImage);
    }

    public Optional<CarouselImage> getByArticle(Article article) {
        return carouselImageRepository.findByArticle(article);
    }
}
