import cv2
from skimage.metrics import mean_squared_error
from skimage.metrics import peak_signal_noise_ratio
import numpy as np

original_test_image_names = {
    'Lenna' : 'Lenna_test_image.png',
    'Baboon' : 'Baboon_test_image.png',
    'Peppers' : 'Peppers_test_image.png'
}

stego_images = {
    'Lenna': [
        'Lenna_replacement.png         ',
        'Lenna_matching.png            ',
        'Lenna_matching_revisited.png  ',
        'Lenna_hamming_codes.png       ',
        'Lenna_random_selection.png    ',
        'Lenna_multi_bit.png           '
    ],
    'Baboon': [
        'Baboon_replacement.png        ',
        'Baboon_matching.png           ',
        'Baboon_matching_revisited.png ',
        'Baboon_hamming_codes.png      ',
        'Baboon_random_selection.png   ',
        'Baboon_multi_bit.png          '
    ],
    'Peppers': [
        'Peppers_replacement.png       ',
        'Peppers_matching.png          ',
        'Peppers_matching_revisited.png',
        'Peppers_hamming_codes.png     ',
        'Peppers_random_selection.png  ',
        'Peppers_multi_bit.png         '
    ]
}

def mse(cover_image, stego_image):
    return mean_squared_error(cover_image, stego_image)

def psnr(cover_image, stego_image):
    return peak_signal_noise_ratio(cover_image, stego_image)

if __name__ == "__main__":

    for original_image_name in original_test_image_names.keys():

        print("Test ----------> " + original_image_name)
        cover_image = cv2.imread("/home/daniel/licenta/steg0vault/src/main/algorithms_MSE_PSNR_Tests/test_images/" + original_test_image_names[original_image_name])

        for stego_image_name in stego_images[original_image_name]:
            stego_image = cv2.imread("/home/daniel/licenta/steg0vault/src/main/algorithms_MSE_PSNR_Tests/test_images/STEGO_IMAGES/" + original_image_name + "_stego/" + stego_image_name.strip())

            MSE_VALUE = mse(cover_image, stego_image)
            PSNR_VALUE = psnr(cover_image, stego_image)
            print(stego_image_name + " MSE: " + str(MSE_VALUE) + " | PSNR: " + str(PSNR_VALUE))

    # for i in range(1, 7):
    # cover_image = cv2.imread("/home/daniel/licenta/steg0vault/src/main/algorithms_MSE_PSNR_Tests/test_images/Lenna_test_image.png")
    # stego_image = cv2.imread("/home/daniel/licenta/steg0vault/src/main/algorithms_MSE_PSNR_Tests/test_images/hasd.png")
    #
    # print(mse(cover_image, stego_image))
    #
    # diff = np.subtract(cover_image, stego_image)
    #
    # cv2.imshow('Difference', diff)
    #
    # # waiting using waitKey method
    # cv2.waitKey(0)
    # cv2.destroyAllWindows()
